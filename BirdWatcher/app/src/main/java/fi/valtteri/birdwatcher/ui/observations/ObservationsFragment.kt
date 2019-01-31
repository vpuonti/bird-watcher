package fi.valtteri.birdwatcher.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import fi.valtteri.birdwatcher.R

class ObservationsFragment : Fragment() {

    companion object {
        fun newInstance() = ObservationsFragment()
    }

    private lateinit var viewModel: ObservationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.observations_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ObservationsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
