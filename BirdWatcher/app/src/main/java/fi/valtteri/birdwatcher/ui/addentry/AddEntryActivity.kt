package fi.valtteri.birdwatcher.ui.addentry

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.android.AndroidInjection
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.location.LocationService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
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

    lateinit var speciesAdapter: SpeciesSelectionAdapter
    lateinit var speciesInput: SpeciesSelectionEditText

    lateinit var descriptionInput: EditText
    lateinit var descriptionInputLayout: TextInputLayout

    lateinit var raritySpinner: Spinner
    lateinit var rarityAdapter: ArrayAdapter<String>

    lateinit var permissionsRationaleDialog: AlertDialog
    lateinit var speciesSelectionDialog: AlertDialog

    lateinit var entryFileName: String
    private var entryFile: File? = null
    private var currentPicUri: Uri? = null

    lateinit var saveButton: MaterialButton

    lateinit var locationProgressBar: ProgressBar

    private val compositeDisposable = CompositeDisposable()

    private var locationAllowed: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private var currentObservationPicUri: Uri? = null
    private var picIsSaved: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

        collapsing_toolbar.title = resources.getText(R.string.new_observation)
        camera_fab.setOnClickListener(this::handleFabClick)

        //init dialogs
        initializeDialogs()

        // setup species input
        speciesInput = species_input
        speciesInput.isEnabled = true
        speciesInput.setOpenSpeciesSelection(object : SpeciesSelectionEditText.OpenSpeciesSelection {
            override fun open() {
                speciesSelectionDialog.show()
            }

        })


        locationProgressBar = progress_horizontal

        saveButton = save_button
        saveButton.setOnClickListener(this::handleSaveClick)

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

        viewModel.getSpecies().observe(this, Observer {birds ->
            speciesAdapter.setItems(birds)
            speciesInput.hint="Select from ${birds.size} species"
        })

        viewModel.getEntryTimestamp().observe(this, Observer { timestamp ->
            timestamp_input.setText(timestamp.toString(DateTimeFormat.mediumDateTime()), TextView.BufferType.NORMAL)
        })

        viewModel.getEntrySpecies().observe(this, Observer { birdObservable ->
            if(!birdObservable.isPresent) {
                species_input_container.error = "Add species"
            } else {
                species_input_container.error = null
                val bird = birdObservable.get()
                speciesInput.setText(bird.displayName, TextView.BufferType.NORMAL)
            }

        })

        viewModel.getEntryDescription().observe(this, Observer { descriptionObservable ->
            if (!descriptionObservable.isPresent) {
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
            val lat = location.latitude
            val lng = location.longitude
            latitude_input.setText(lat.toString(), TextView.BufferType.NORMAL)
            longitude_input.setText(lng.toString(), TextView.BufferType.EDITABLE)
            viewModel.setEntryLatLng(lat, lng)

        })



        viewModel.isSaveAllowed().observe(this, Observer { saveAllowed ->
            saveButton.isEnabled = saveAllowed
        })

        locationAllowed.subscribe { allowed ->
            if(allowed) {
                latitude_input.visibility = View.VISIBLE
                longitude_input.visibility = View.VISIBLE
            } else {
                latitude_input.visibility = View.GONE
                longitude_input.visibility = View.GONE
            }
        }

        handleLocationPermissions()
        descriptionInput.addTextChangedListener { it?.let { editable ->  viewModel.setEntryDescription(editable.toString()) } }


    }

    private fun checkLocationLoadingStatus() {
        // get location loading status
        val disposable = locationService.getLocationLoadingStatus()
            .doOnSubscribe {
                locationProgressBar.visibility = View.VISIBLE
            }
            .subscribe(
                {
                    Snackbar.make(activity_container, "Location loaded!", Snackbar.LENGTH_LONG)
                        .show()

                    locationProgressBar.visibility = View.GONE
                },
                {
                    locationProgressBar.visibility = View.GONE
                    Snackbar.make(activity_container, "${it.message}", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry") { checkLocationLoadingStatus() }
                        .show()
                })
        compositeDisposable.add(disposable)
    }


    private fun sendLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this@AddEntryActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            AddEntryActivity.FINE_LOCATION_REQUEST_CODE
        )

    }

    private fun handleLocationPermissions() {
        //check location permissions
        if(ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            locationAllowed.onNext(false)
            if(ActivityCompat.shouldShowRequestPermissionRationale(
                    this@AddEntryActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //show rationale to user
                Timber.d("Showing rationale to user")
                permissionsRationaleDialog.show()

            } else {
                sendLocationPermissionRequest()
            }
        } else {
            //we have permissions
            locationAllowed.onNext(true)
            Timber.d("We have permissions")
            startLocationService()

        }

    }

    private fun initializeDialogs() {
        //permissions rationale dialog
        permissionsRationaleDialog = AlertDialog.Builder(this@AddEntryActivity)
            .setMessage(R.string.location_rationale)
            .setPositiveButton("Yes") { dialog, id ->
                sendLocationPermissionRequest()
            }
            .setNeutralButton("Cancel") {dialog, id ->
                //finish()
            }
            .setCancelable(true)
            .setOnCancelListener {
                //finish()
            }
            .create()

        //species selection dialog

        // setup species adapter
        speciesAdapter = SpeciesSelectionAdapter(this, android.R.layout.simple_selectable_list_item, mutableListOf())

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
                    viewModel.setEntrySpecies(it)

                }
            }
        }

        val searchView = alertView.species_search
        searchView.setIconifiedByDefault(false)
        //searchView.isIconified = false
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

        val closeFab = alertView.fab_close_species_selection
        builder.setView(alertView)
        speciesSelectionDialog = builder.create()
        speciesSelectionDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        closeFab.setOnClickListener {
            speciesSelectionDialog.dismiss()
        }

    }

    private fun startLocationService() {
        lifecycle.addObserver(locationService)
        checkLocationLoadingStatus()
    }


    private fun handleFabClick(view: View) {
       dispatchTakePictureIntent()
    }

    private fun handleSaveClick(view: View) {
        locationProgressBar.visibility = View.VISIBLE
        saveButton.isEnabled = false

        val d = viewModel.saveObservation()
            .subscribe(
            {
                picIsSaved = true
                finish()
            },
            {
                Snackbar.make(activity_container, "Error saving observation!", Snackbar.LENGTH_LONG).show()
                it.printStackTrace()
                locationProgressBar.visibility = View.GONE
                saveButton.isEnabled = true

            }
        )
        compositeDisposable.add(d)
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
                        it)
                    currentPicUri = photoUri
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
                    handleLocationPermissions()
                } else {
                    Timber.d("User denied location permission.")
                }
            }
        }
    }

    private fun deletePicture(uri: Uri) {
        val file = File(uri.path)
        file.delete()
    }



    override fun onPause() {
        super.onPause()
        speciesSelectionDialog.dismiss()
        permissionsRationaleDialog.dismiss()
    }

    override fun onDestroy() {
        if(!picIsSaved) {
            currentPicUri?.also { deletePicture(it) }
        }
        speciesSelectionDialog.dismiss()
        permissionsRationaleDialog.dismiss()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    companion object {
        private const val PICTURE_REQUEST_CODE = 1019
        private const val FINE_LOCATION_REQUEST_CODE = 123
    }

}
