package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.widget.GridLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.SettingsRepository
import fi.valtteri.birdwatcher.data.SpeciesRepository
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(
    private val speciesRepository: SpeciesRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val speciesData: MutableLiveData<List<Species>> = MutableLiveData()
    private val speciesNames: MutableLiveData<List<String>> = MutableLiveData()


    // data to make observation
    private var entryTimestamp: DateTime? = null
    private var entryPicFileName: String? = null
    private var rarity: ObservationRarity? = null

    fun getSpecies(): LiveData<List<Species>> {
        scope.launch(Dispatchers.IO) {
            speciesRepository.getSpecies().subscribe { it ->
                speciesData.postValue(it)
            }
        }
        return speciesData
    }

    fun getSpeciesNames() : LiveData<List<String>> {
        scope.launch(Dispatchers.IO) {
            settingsRepository.getLanguagePref()
                .switchMap { mapSpeciesToNames(it) }
                .subscribe { speciesNames.postValue(it) }

        }
        return speciesNames
    }

    private fun mapSpeciesToNames(language: String): Flowable<List<String>> {
        val languages = context.resources.getStringArray(R.array.species_language_choices)
        return speciesRepository.getSpecies()
            .map { it ->
                return@map it.map { species ->
                    when (languages.indexOf(language)) {
                        0 -> {
                            species.scienticName
                        }
                        1 -> {
                            species.finnishName
                        }
                        2 -> {
                            species.englishName
                        }
                        3 -> {
                            species.swedishName
                        }
                        else -> {
                            throw IllegalArgumentException("Invalid language: $language")
                        }
                    }
                }
            }
    }


    fun initializeNewEntry(){
        entryTimestamp = DateTime.now()
        entryPicFileName = "${entryTimestamp?.millis}_bird.jpg"

    }

    fun setEntryRarity(rarity: ObservationRarity) {
        this.rarity = rarity
    }

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
    }

}
