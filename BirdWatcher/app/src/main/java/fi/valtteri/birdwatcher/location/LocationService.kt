package fi.valtteri.birdwatcher.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.RuntimeException
import javax.inject.Inject

class LocationService @Inject constructor(context: Context) : LifecycleObserver, LocationCallback() {
    private val job = Job()

    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    //private val locationData: MutableLiveData<Location?> = MutableLiveData()
    private val fusedLocationProvider = FusedLocationProviderClient(context)

    private val locationData: BehaviorSubject<Location> = BehaviorSubject.create()


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {

        //get last location so we don't have to wait for LocationCallback to start doing work
        scope.launch {
             try {
                val lastLocation: Location? = withContext(Dispatchers.IO) { Tasks.await(fusedLocationProvider.lastLocation) }
                lastLocation?.also { location ->
                    Timber.d("Initializing location data with Lat: ${location.latitude} Lng: ${location.longitude}")
                    locationData.onNext(location)
                }
            } catch (e: SecurityException) {
                Timber.e("Error getting last location: $e")
            }
        }
        val locationRequest = LocationRequest.create()
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setInterval(5000)
        try {
            fusedLocationProvider.requestLocationUpdates(locationRequest, this, Looper.myLooper())
            Timber.d("Started requesting location updates")

        } catch (e: SecurityException) {
            Timber.e("Error requesting location updates: $e")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stop() {
        job.cancelChildren()
        fusedLocationProvider.removeLocationUpdates(this)

        Timber.d("Stopped location service")
    }


    fun getLocation(): LiveData<Location> = LiveDataReactiveStreams.fromPublisher(locationData.toFlowable(BackpressureStrategy.LATEST))

    fun getLocationLoadingStatus() : Completable = Completable.create { emitter ->

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!gpsEnabled && !networkEnabled) {
            emitter.onError(RuntimeException("Location seems to be disabled"))
        }
        locationData.firstOrError()
            .doOnError {
                Timber.d("Error loading first location")
                emitter.onError(it)
            }
            .doOnSuccess {
                Timber.d("First location: $it")
                emitter.onComplete()
            }
            .subscribe()
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation?.let { locationData.onNext(it) }
    }

}