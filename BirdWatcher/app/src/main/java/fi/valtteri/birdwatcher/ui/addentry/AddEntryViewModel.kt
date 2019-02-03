package fi.valtteri.birdwatcher.ui.addentry

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel;
import fi.valtteri.birdwatcher.data.Repository
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(val repository: Repository) : ViewModel() {


    fun getSpecies() = LiveDataReactiveStreams.fromPublisher(repository.getSpecies())


}
