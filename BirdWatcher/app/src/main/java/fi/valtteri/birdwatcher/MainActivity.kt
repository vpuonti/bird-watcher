package fi.valtteri.birdwatcher

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import fi.valtteri.birdwatcher.ui.addentry.AddEntryActivity
import fi.valtteri.birdwatcher.ui.main.MainFragment
import fi.valtteri.birdwatcher.ui.observations.ObservationsFragment
import fi.valtteri.birdwatcher.ui.settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    HasSupportFragmentInjector,
        BottomNavigationView.OnNavigationItemSelectedListener
{
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.bottom_navigation_observations
        fab_observation.setOnClickListener(this::handleFabClick)


    }

    fun fragmentLoadingReady() {
        when(progress_horizontal.visibility) {
            View.VISIBLE -> {
                progress_horizontal.visibility = View.GONE
            }
        }

    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.d("Selected ${item.title}")
        progress_horizontal.visibility = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        when (item.title) {
            resources.getText(R.string.main) -> {
                transaction.replace(main_view.id, MainFragment.newInstance())
            }
            resources.getText(R.string.observations) -> {
                transaction.replace(main_view.id, ObservationsFragment.newInstance())

            }
            resources.getText(R.string.settings) -> {
                transaction.replace(main_view.id, SettingsFragment.newInstance())
            }
        }
        transaction.commit()
        return true
    }





    private fun handleFabClick(view: View) {
        val intent = Intent(this, AddEntryActivity::class.java)
        progress_horizontal.visibility = View.VISIBLE
        startActivityForResult(intent, START_OBSERVATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        progress_horizontal.visibility = View.GONE

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.observationview_menu, menu)
//        return true
//    }


    companion object {
        private const val START_OBSERVATION = 11212
    }



}
