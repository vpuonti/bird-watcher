package fi.valtteri.birdwatcher.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class LocationService @Inject constructor(context: Context) : LifecycleObserver, LocationCallback() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val locationData: MutableLiveData<Location?> = MutableLiveData()
    private val fusedLocationProvider = FusedLocationProviderClient(context)


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        scope.launch {
            try {
                val lastLocation = Tasks.await(fusedLocationProvider.lastLocation)
                Timber.d("Initializing location data")
                locationData.postValue(lastLocation)

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

    fun getLocation(): LiveData<Location?> = locationData


    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation?.let { locationData.value = it }
    }


}