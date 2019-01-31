package fi.valtteri.birdwatcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjection
import fi.valtteri.birdwatcher.location.LocationService
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var locationService: LocationService

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        askForPermissions()
        lifecycle.addObserver(locationService)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.d("Selected ${item.title}")
        return true
    }

    private fun askForPermissions() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        if(ContextCompat.checkSelfPermission(
                applicationContext,
                fineLocationPermission) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity, fineLocationPermission)) {
                //show rationale to user
                Timber.d("Showing rationale to user")
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(fineLocationPermission),
                    PERMISSION_REQUEST_FINE_LOCATION)

            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                TODO("FINISH PPERMISSION ASKING")
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }

}
