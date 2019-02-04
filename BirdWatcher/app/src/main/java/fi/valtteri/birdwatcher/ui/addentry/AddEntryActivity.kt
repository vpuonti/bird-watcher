package fi.valtteri.birdwatcher.ui.addentry

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import fi.valtteri.birdwatcher.R
import kotlinx.android.synthetic.main.activity_add_entry.*
import kotlinx.android.synthetic.main.content_add_entry.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject


class AddEntryActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddEntryViewModel

    lateinit var adapter: ArrayAdapter<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsing_toolbar.title = resources.getText(R.string.new_observation)

        adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, mutableListOf())
        input_observation_species.setAdapter(adapter)
        input_observation_species.threshold = 1

        fab.setOnClickListener(this::handleFabClick)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddEntryViewModel::class.java)
        viewModel.initializeNewEntry()

        viewModel.getSpeciesNames().observe(this, Observer {names ->
            adapter.clear()
            adapter.addAll(names)
            Timber.d("Adding ${names.size} names to species autocompletetextview")
        })


    }


    private fun handleFabClick(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, PICTURE_REQUEST_CODE)
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

    companion object {
        private const val PICTURE_REQUEST_CODE = 1019
    }

}
