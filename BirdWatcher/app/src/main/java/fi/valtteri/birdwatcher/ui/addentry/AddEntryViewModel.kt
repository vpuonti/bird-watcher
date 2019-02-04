package fi.valtteri.birdwatcher.ui.addentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.data.SpeciesRepository
import fi.valtteri.birdwatcher.data.entities.Species
import kotlinx.coroutines.*
import org.joda.time.DateTime
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(private val speciesRepository: SpeciesRepository) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val mSpeciesData: MutableLiveData<List<Species>> = MutableLiveData()

    private var entryTimestamp: DateTime? = null

    private var entryPicFileName: String? = null

    fun getSpecies(): LiveData<List<Species>> {
        scope.launch(Dispatchers.IO) {
            speciesRepository.getSpecies().subscribe { it ->
                mSpeciesData.value
            }
        }
        return mSpeciesData
    }


    fun initializeNewEntry() {
        entryTimestamp = DateTime.now()
        entryPicFileName = "${entryTimestamp?.millis}_bird.jpg"

    }

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
    }

}
