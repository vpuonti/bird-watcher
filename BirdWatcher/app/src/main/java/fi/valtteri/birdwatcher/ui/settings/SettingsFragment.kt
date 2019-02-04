package fi.valtteri.birdwatcher.ui.settings

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.Observer
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

    private lateinit var adapter: ArrayAdapter<CharSequence>
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)

        val view = inflater.inflate(R.layout.settings_fragment, container, false)

        adapter = ArrayAdapter.createFromResource(context, R.array.species_language_choices, R.layout.support_simple_spinner_dropdown_item)
        spinner = view.species_language_spinner
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)
        viewModel.getCurrentLangSetting().observe(this, Observer {
            val pos = adapter.getPosition(it)
            spinner.tag = pos
            spinner.setSelection(pos)
        })

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        adapter.getItem(position)?.toString()?.let {
            if(spinner.tag != position) {
                viewModel.setCurrentLangSetting(it)
            }

        }
    }

}
