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
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
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

    lateinit var descriptionInput: EditText
    lateinit var descriptionInputLayout: TextInputLayout

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

        Timber.d("ONCREATE")
        // setup species adapter
        speciesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        // setup species input
        speciesInput = species_input
        addSpeciesBtn = add_species_btn
        addSpeciesBtn.setOnClickListener(this::handleSpeciesSelectionClick)

        //setup rarity spinner
        raritySpinner = rarity_spinner
        rarityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ObservationRarity.values().map { it.toString() })
        raritySpinner.adapter = rarityAdapter
        raritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setEntryRarity(ObservationRarity.values()[position])
            }
        }

        //setup description box
        descriptionInput = description_input
        descriptionInputLayout = description_input_container


        //get viewmodel
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddEntryViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.initializeNewEntry()

        }

        viewModel.getSpeciesNames().observe(this, Observer {names ->
            speciesAdapter.clear()
            speciesAdapter.addAll(names)
        })

        viewModel.getEntryTimestamp().observe(this, Observer { timestamp ->
            timestamp_input.setText(timestamp.toString(DateTimeFormat.mediumDateTime()), TextView.BufferType.NORMAL)
        })

        viewModel.getEntrySpeciesName().observe(this, Observer { name ->
            speciesInput.setText(name, TextView.BufferType.NORMAL)
            if(name.isBlank()) {
                species_input_container.error = "Add species"
            } else {
                species_input_container.error = null
            }
        })

        viewModel.getEntryDescription().observe(this, Observer { description ->
            Timber.d("Desc: $description")
            if (description.isBlank()) {
                descriptionInputLayout.error = "Add description"
            } else {
                descriptionInputLayout.error = null
            }
        })

        // observe location
        locationService.getLocation().observe(this, Observer { location ->
            latitude_input.setText(location.latitude.toString(), TextView.BufferType.NORMAL)
            longitude_input.setText(location.longitude.toString(), TextView.BufferType.EDITABLE)
        })

        askForLocationPermissions()
        descriptionInput.addTextChangedListener { it?.let { editable ->  viewModel.setEntryDescription(editable.toString()) } }


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
                    .setNeutralButton("Cancel") {dialog, id ->
                        finish()
                    }
                    .setCancelable(true)
                    .setOnCancelListener { finish() }
                    .create()
                    .show()

            } else {
                requestLocationPermission()
            }
        } else {
            //we have permissions
            Timber.d("We have permissions")
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
       dispatchTakePictureIntent()
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, PICTURE_REQUEST_CODE)
            }
        }

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
                    viewModel.setSpecies(it)

                }
            }

        }
        dialog.show()


    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
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
                    finish()
                }
            }
        }
    }


    companion object {
        private const val PICTURE_REQUEST_CODE = 1019
        private const val FINE_LOCATION_REQUEST_CODE = 123
    }

}
