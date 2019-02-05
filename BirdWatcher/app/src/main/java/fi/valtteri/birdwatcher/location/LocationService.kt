package fi.valtteri.birdwatcher.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LocationService @Inject constructor(
    private val context: Context) : LifecycleObserver, LocationCallback() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val locationData: MutableLiveData<Location> = MutableLiveData()
    private val fusedLocationProvider = FusedLocationProviderClient(context)


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        scope.launch {
            try {
                fusedLocationProvider.lastLocation.result?.let { locationData.postValue(it) }

            } catch (e: SecurityException) {
                Timber.e("Error getting last location: $e")
            }
        }
        val locationRequest = LocationRequest.create()
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setInterval(1000)
        try {
            fusedLocationProvider.requestLocationUpdates(locationRequest, this)
            Timber.d("Started requesting location updates")

        } catch (e: SecurityException) {
            Timber.e("Error requesting location updates: $e")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stop() {

        Timber.d("Stopped location service")
    }

    fun getLocation(): LiveData<Location> = locationData


    override fun onLocationResult(locationResult: LocationResult?) {

    }

//    override fun onLocationChanged(location: Location?) {
//        Timber.d("Location changed")
//        location?.let { locationData.value = it }
//    }
//
//    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        Timber.d("Status changed")
//    }
//
//    override fun onProviderEnabled(provider: String?) {
//        provider?.let {
//            when(it) {
//                LocationManager.GPS_PROVIDER -> {
//                    Timber.d("Provider ${LocationManager.GPS_PROVIDER} enabled")
//                    gpsEnabled.value = true
//                }
//            }
//        }
//
//    }
//
//    override fun onProviderDisabled(provider: String?) {
//        Timber.d("Provider $provider disabled")
//        provider?.let {
//            when(it) {
//                LocationManager.GPS_PROVIDER -> {
//                    Timber.d("Provider ${LocationManager.GPS_PROVIDER} disabled")
//                    gpsEnabled.value = true
//                }
//            }
//        }
//    }

}