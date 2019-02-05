package fi.valtteri.birdwatcher.ui.addentry

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import dagger.android.AndroidInjection
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.location.LocationService
import kotlinx.android.synthetic.main.activity_add_entry.*
import kotlinx.android.synthetic.main.content_add_entry.*
import kotlinx.android.synthetic.main.species_selector_layout.view.*
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import java.io.File
import java.io.IOException
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

    lateinit var permissionsRationaleDialog: AlertDialog
    lateinit var speciesSelectionDialog: AlertDialog

    lateinit var entryFileName: String
    private var entryFile: File? = null
    private var currentPicUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        // set keyboard toggle
        //.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsing_toolbar.title = resources.getText(R.string.new_observation)
        camera_fab.setOnClickListener(this::handleFabClick)

        //init dialogs
        initializeDialogs()
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

        viewModel.getEntryPicFileName().observe(this, Observer { entryFileName = it })

        viewModel.getEntryPicUri().observe(this, Observer { uri ->
            currentPicUri = uri
            Glide.with(this)
                .load(uri)
                .into(collapsing_imageview)
        })

        // observe location
        locationService.getLocation().observe(this, Observer { location ->
            latitude_input.setText(location.latitude.toString(), TextView.BufferType.NORMAL)
            longitude_input.setText(location.longitude.toString(), TextView.BufferType.EDITABLE)
        })

        viewModel.isSaveAllowed().observe(this, Observer { saveAllowed ->
            save_button.isEnabled = saveAllowed
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
                permissionsRationaleDialog.show()

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

    private fun initializeDialogs() {
        //permissions rationale dialog
        permissionsRationaleDialog = AlertDialog.Builder(this@AddEntryActivity)
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

        //species selection dialog

        // setup species adapter
        speciesAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        // setup species input
        speciesInput = species_input

        val builder = AlertDialog.Builder(this@AddEntryActivity)

        //build custom layout
        val alertView = LayoutInflater.from(this@AddEntryActivity).inflate(R.layout.species_selector_layout, null)
        val listView = alertView.species_list
        listView.adapter = speciesAdapter
        listView.isTextFilterEnabled = true
        listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                speciesSelectionDialog.dismiss()
                speciesAdapter.getItem(position)?.let {
                    viewModel.setSpecies(it)

                }
            }

        }

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
        speciesSelectionDialog = builder.create()

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

                val photoFile: File? = try {
                    createPictureFile()
                } catch (ex: IOException) {
                    Timber.e("Error creating file: $ex")
                    null
                }
                photoFile?.also {
                        val photoUri: Uri = FileProvider.getUriForFile(
                            this,
                            "fi.valtteri.birdwatcher.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(takePictureIntent, PICTURE_REQUEST_CODE)
                    }

            }
        }

    }

    private fun createPictureFile() : File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            entryFileName,
            ".jpg",
            storageDir
        )
        entryFile = file
        return file
    }


    private fun handleSpeciesSelectionClick(view: View) {
        speciesSelectionDialog.show()

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            entryFile?.also { file ->
                //new pic uri
                val uri = Uri.fromFile(file)
                //delete old pic
                currentPicUri?.also { oldPicUri ->
                    val oldPic = File(oldPicUri.path)
                    oldPic.delete()
                }

                viewModel.setPictureUri(uri)

            }
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


    override fun onPause() {
        super.onPause()
        speciesSelectionDialog.dismiss()
        permissionsRationaleDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        speciesSelectionDialog.dismiss()
        permissionsRationaleDialog.dismiss()
    }

    companion object {
        private const val PICTURE_REQUEST_CODE = 1019
        private const val FINE_LOCATION_REQUEST_CODE = 123
    }

}
