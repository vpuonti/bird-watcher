package fi.valtteri.birdwatcher.ui.settings

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection

import fi.valtteri.birdwatcher.R
import kotlinx.android.synthetic.main.settings_fragment.view.*
import javax.inject.Inject

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    companion object {

        fun newInstance() = SettingsFragment()
    }
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)

        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        view.species_language_spinner.adapter = ArrayAdapter.createFromResource(context, R.array.species_language_choices, R.layout.support_simple_spinner_dropdown_item)
        view.species_language_spinner.onItemSelectedListener = this
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        view?.let {
            Snackbar.make(it, "Nothing selected", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

}
