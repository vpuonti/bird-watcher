package fi.valtteri.birdwatcher.ui.observations

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection

import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.di.BirdWatcherViewModelFactory
import kotlinx.android.synthetic.main.observations_fragment.view.*
import timber.log.Timber
import javax.inject.Inject

class ObservationsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ObservationsViewModel
    private lateinit var adapter: ObservationsAdapter
    private lateinit var recyclerview: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.observations_fragment, container, false)
        adapter = ObservationsAdapter()
        recyclerview = view.observation_recycler
        layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = layoutManager
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ObservationsViewModel::class.java)
        viewModel.getObservations().observe(this, Observer { observables ->
            val obs = observables.map { it.timeStamp.millis }
            Timber.d("$obs")
            adapter.setItems(observables)
        })
    }

    companion object {
        fun newInstance() = ObservationsFragment()
    }

}
