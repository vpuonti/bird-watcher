package fi.valtteri.birdwatcher.ui.addentry

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import dagger.android.AndroidInjection
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.location.LocationService
import kotlinx.android.synthetic.main.activity_add_entry.*
import kotlinx.android.synthetic.main.content_add_entry.*
import kotlinx.android.synthetic.main.species_selector_layout.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject


class AddEntryActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var locationService: LocationService

    lateinit var viewModel: AddEntryViewModel

    lateinit var speciesAdapter: ArrayAdapter<String>

    lateinit var speciesInput: EditText
    lateinit var addSpeciesBtn: MaterialButton

    lateinit var raritySpinner: Spinner
    lateinit var rarityAdapter: ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsing_toolbar.title = resources.getText(R.string.new_observation)
        camera_fab.setOnClickListener(this::handleFabClick)

        speciesAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, mutableListOf())
        // setup species input
        speciesInput = species_input
        addSpeciesBtn = add_species_btn
        addSpeciesBtn.setOnClickListener(this::handleSpeciesSelectionClick)

        //setup rarity spinner
        raritySpinner = rarity_spinner
        rarityAdapter = ArrayAdapter(this, android.R.layout.select_dialog_item, ObservationRarity.values().map { it.toString() })
        raritySpinner.adapter = rarityAdapter
        raritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setEntryRarity(ObservationRarity.values()[position])
            }

        }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddEntryViewModel::class.java)
        val timestamp = DateTime.now()
        viewModel.initializeNewEntry()
        timestamp_input.setText(timestamp.toString(DateTimeFormat.mediumDateTime()), TextView.BufferType.NORMAL)

        viewModel.getSpeciesNames().observe(this, Observer {names ->
            speciesAdapter.clear()
            speciesAdapter.addAll(names)
        })

        askForLocationPermissions()

    }


    private fun askForLocationPermissions() {
        //check location permissions
        if(ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this@AddEntryActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //show rationale to user
                Timber.d("Showing rationale to user")
                AlertDialog.Builder(this@AddEntryActivity)
                    .setMessage(R.string.location_rationale)
                    .setPositiveButton("Yes") { dialog, id ->
                        requestLocationPermission()
                    }
                    .setNegativeButton("No") { dialog, id ->
                        Toast.makeText(this@AddEntryActivity, "Location won't be used", Toast.LENGTH_LONG).show()
                    }
                    .create()
                    .show()

            } else {
                requestLocationPermission()
            }
        } else {
            //we have permissions
            startLocationService()

        }

    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this@AddEntryActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            AddEntryActivity.FINE_LOCATION_REQUEST_CODE
        )

    }

    private fun startLocationService() {
        lifecycle.addObserver(locationService)
    }


    private fun handleFabClick(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, PICTURE_REQUEST_CODE)
    }

    private fun handleSpeciesSelectionClick(view: View) {
        val builder = AlertDialog.Builder(this@AddEntryActivity)

        //build custom layout
        val alertView = LayoutInflater.from(this@AddEntryActivity).inflate(R.layout.species_selector_layout, null)
        val listView = alertView.species_list
        listView.adapter = speciesAdapter
        listView.isTextFilterEnabled = true
        listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE



        val searchView = alertView.species_search
        searchView.isIconified = false
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if(newText.isBlank()) {
                        listView.clearTextFilter()
                    } else {
                        listView.setFilterText(it)
                    }
                }
                return true
            }

        })
        builder.setView(alertView)
        val dialog = builder.create()

        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                dialog.dismiss()
                speciesAdapter.getItem(position)?.let {
                    onSpeciesSelected(it)

                }
            }

        }
        dialog.show()


    }

    private fun onSpeciesSelected(speciesName: String) {
        speciesInput.setText(speciesName, TextView.BufferType.NORMAL)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 50, stream)
            val byteArray = stream.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(
                byteArray, 0,
                byteArray.size
            )

            collapsing_imageview.setImageBitmap(bitmap)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            AddEntryActivity.FINE_LOCATION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("User granted location permissions")
                    startLocationService()

                } else {
                    Timber.d("User denied location permission.")
                }
            }
        }
    }

    companion object {
        private const val PICTURE_REQUEST_CODE = 1019
        private const val FINE_LOCATION_REQUEST_CODE = 123
    }

}
