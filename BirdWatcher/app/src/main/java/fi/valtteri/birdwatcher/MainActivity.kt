package fi.valtteri.birdwatcher

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import fi.valtteri.birdwatcher.ui.addentry.AddEntryActivity
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
        camera_fab.setOnClickListener(this::handleFabClick)


    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.d("Selected ${item.title}")
        val transaction = supportFragmentManager.beginTransaction()
        when (item.title) {
            resources.getText(R.string.main) -> {

            }
            resources.getText(R.string.observations) -> {

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
        startActivity(intent)
    }




}
