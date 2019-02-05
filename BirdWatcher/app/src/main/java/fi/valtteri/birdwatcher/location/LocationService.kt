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
import timber.log.Timber
import javax.inject.Inject

class LocationService @Inject constructor(
    private val context: Context) : LifecycleObserver, LocationListener {

    private val locationData: MutableLiveData<Location> = MutableLiveData()
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val gpsEnabled: MutableLiveData<Boolean> = MutableLiveData()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun start() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            Timber.d("Started requesting location updates")

        } catch (e: SecurityException) {
            Timber.e("No permissions! $e")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun stop() {
        locationManager.removeUpdates(this)
        Timber.d("Stopped location service")
    }

    fun getLocation(): LiveData<Location> = locationData

    fun gpsEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    override fun onLocationChanged(location: Location?) {
        Timber.d("Location changed")
        location?.let { locationData.value = it }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("Status changed")
    }

    override fun onProviderEnabled(provider: String?) {
        provider?.let {
            when(it) {
                LocationManager.GPS_PROVIDER -> {
                    Timber.d("Provider ${LocationManager.GPS_PROVIDER} enabled")
                    gpsEnabled.value = true
                }
            }
        }

    }

    override fun onProviderDisabled(provider: String?) {
        Timber.d("Provider $provider disabled")
        provider?.let {
            when(it) {
                LocationManager.GPS_PROVIDER -> {
                    Timber.d("Provider ${LocationManager.GPS_PROVIDER} disabled")
                    gpsEnabled.value = true
                }
            }
        }
    }

}