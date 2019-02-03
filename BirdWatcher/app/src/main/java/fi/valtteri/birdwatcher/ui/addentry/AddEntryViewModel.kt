package fi.valtteri.birdwatcher.ui.addentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.data.Repository
import fi.valtteri.birdwatcher.data.entities.Species
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(private val repository: Repository) : ViewModel() {


    fun getSpecies(): LiveData<List<Species>> = LiveDataReactiveStreams.fromPublisher(repository.getSpecies())



}
