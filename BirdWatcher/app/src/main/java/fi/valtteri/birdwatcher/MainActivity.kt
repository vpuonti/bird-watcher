package fi.valtteri.birdwatcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import fi.valtteri.birdwatcher.location.LocationService
import fi.valtteri.birdwatcher.ui.addentry.AddEntryFragment
import fi.valtteri.birdwatcher.ui.observations.ObservationsFragment
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    HasSupportFragmentInjector,
    BottomNavigationView.OnNavigationItemSelectedListener
{
    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var observationsFragment: ObservationsFragment

    @Inject
    lateinit var addEntryFragment: AddEntryFragment

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        fab.setOnClickListener(this::handleFabClick)

        //check location permissions
        if(ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //show rationale to user
                Timber.d("Showing rationale to user")

            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_FINE_LOCATION)
            }
        } else {
            //we have permissions
            startLocationService()

        }

    }

    private fun startLocationService() {
        lifecycle.addObserver(locationService)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.d("Selected ${item.title}")
        return true
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("User granted location permissions")
                    startLocationService()

                } else {
                    Timber.d("User denied location permission.")
                }
            }
        }
    }

    private fun handleFabClick(view: View) {
        supportFragmentManager.beginTransaction()
            .add(main_view.id, addEntryFragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }

}
