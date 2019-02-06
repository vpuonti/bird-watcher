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
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class LocationService @Inject constructor(context: Context) : LifecycleObserver, LocationCallback() {
    private val job = Job()

    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var noGpsCallback: LocationServiceNotAvailableOnStartUpCallback? = null

    //private val locationData: MutableLiveData<Location?> = MutableLiveData()
    private val fusedLocationProvider = FusedLocationProviderClient(context)
    private val locationData: BehaviorSubject<Location> = BehaviorSubject.create()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        val locationAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!locationAvailable) {
            noGpsCallback?.handleLocationNotAvailableOnStartUp()
        } else {
            //get last location so we don't have to wait for LocationCallback to start doing work
            scope.launch {
                try {
                    val lastLocation = Tasks.await(fusedLocationProvider.lastLocation)
                    Timber.d("Initializing location data")
                    lastLocation?.also { locationData.onNext(it) }

                } catch (e: SecurityException) {
                    Timber.e("Error getting last location: $e")
                }
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


    fun setLocationNotAvailableOnStartUpCallback(
        callback: LocationServiceNotAvailableOnStartUpCallback
    ) {
        noGpsCallback = callback
    }



    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation?.let { locationData.onNext(it) }
    }

    interface LocationServiceNotAvailableOnStartUpCallback {
        fun handleLocationNotAvailableOnStartUp()
    }

}