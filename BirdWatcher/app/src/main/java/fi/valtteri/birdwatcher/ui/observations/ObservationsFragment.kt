package fi.valtteri.birdwatcher.ui.observations

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fi.valtteri.birdwatcher.MainActivity
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Observation
import kotlinx.android.synthetic.main.observations_fragment.view.*
import javax.inject.Inject

class ObservationsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ObservationsViewModel
    private lateinit var adapter: ObservationCardDataAdapter
    private lateinit var recyclerview: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.observations_fragment, container, false)
        adapter = ObservationCardDataAdapter()
        recyclerview = view.observation_recycler
        layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = layoutManager

        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        (activity as MainActivity).fragmentLoadingReady()
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ObservationsViewModel::class.java)
        viewModel.getObservationCardData().observe(this, Observer { observations ->
            adapter.setItems(observations)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.observationview_menu, menu)
        val searchItem = menu.findItem(R.id.observation_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter.filter(it) }
                return true
            }

        })
        searchView.setIconifiedByDefault(true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.filter_ascending -> {
                viewModel.setSortingMode(Observation.SORT_TIMESTAMP_ASCENDING)
            }
            R.id.filter_descending -> {
                viewModel.setSortingMode(Observation.SORT_TIMESTAMP_DESCENDING)
            }
        }
        return true
    }

    companion object {
        fun newInstance() = ObservationsFragment()
    }

}
