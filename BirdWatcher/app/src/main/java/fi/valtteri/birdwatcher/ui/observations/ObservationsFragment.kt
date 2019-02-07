package fi.valtteri.birdwatcher.ui.observations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import fi.valtteri.birdwatcher.MainActivity
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Observation
import kotlinx.android.synthetic.main.observations_fragment.view.*
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject


class ObservationsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ObservationsViewModel
    private lateinit var adapter: ObservationCardDataAdapter
    private lateinit var recyclerview: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var noObservationsSnackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.observations_fragment, container, false)
        noObservationsSnackbar = Snackbar.make(view, "No observations to show.", Snackbar.LENGTH_INDEFINITE)
        adapter = ObservationCardDataAdapter()
        recyclerview = view.observation_recycler
        layoutManager = LinearLayoutManager(context)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = layoutManager
        adapter.setOnOpenLocationClickListener(this::launchMapIntentToLocation)
        setHasOptionsMenu(true)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        (activity as MainActivity).fragmentLoadingReady()
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ObservationsViewModel::class.java)
        viewModel.getObservationCardData().observe(this, Observer { observations ->
            if(observations.isEmpty()){
                noObservationsSnackbar.show()
            } else {
                noObservationsSnackbar.dismiss()
            }
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

    private fun launchMapIntentToLocation(observationCardData: ObservationCardData) {
        val latitude: Double = observationCardData.latitude ?: throw IllegalArgumentException("Given observationCardData has null latitude")
        val longitude: Double = observationCardData.longitude ?: throw IllegalArgumentException("Given observationCardData has null longitude")
        val labelName: String = observationCardData.speciesDisplayName ?: "Observation ${observationCardData.timeStamp.toString(DateTimeFormat.shortDateTime())}"
        val mapsIntentUri = Uri.parse("geo:<$latitude,$longitude?z=15&q=$latitude,$longitude($labelName)")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        context?.let { ctx ->
            if (mapIntent.resolveActivity(ctx.packageManager) != null) {
                startActivity(mapIntent)
            }
        }
    }

    companion object {
        fun newInstance() = ObservationsFragment()
    }

}
