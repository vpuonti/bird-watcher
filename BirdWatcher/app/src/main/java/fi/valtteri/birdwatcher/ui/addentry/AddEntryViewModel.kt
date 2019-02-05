package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.SettingsRepository
import fi.valtteri.birdwatcher.data.SpeciesRepository
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import org.joda.time.DateTime
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
    //private var entryTimestamp: DateTime? = null
    //private var entryPicFileName: String? = null
    private var rarity: ObservationRarity? = null

    private val entryTimestamp: BehaviorSubject<DateTime> = BehaviorSubject.create()
    private val entrySpeciesName: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryDescription: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryPicFileName: Observable<String> = entryTimestamp.map { "${it.millis}_bird.jpg" }

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

    fun getEntryTimestamp() = LiveDataReactiveStreams.fromPublisher(entryTimestamp.toFlowable(BackpressureStrategy.LATEST))

    fun getEntrySpeciesName() = LiveDataReactiveStreams.fromPublisher(entrySpeciesName.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryDescription() = LiveDataReactiveStreams.fromPublisher(entryDescription.toFlowable(BackpressureStrategy.LATEST))

    /**
     * If language: scientific -> map List<Species> to List<String> where String = Species.scientificName
     * Same for all configured languages. (Configured in res/values/strings as <string-array>)
     */
    private fun mapSpeciesToNames(language: String): Flowable<List<String>> {
        val languages = context.resources.getStringArray(R.array.species_language_choices)
        return speciesRepository.getSpecies()
            .map { it ->
                return@map it.map { species ->
                    when (languages.indexOf(language)) {
                        0 -> {
                            species.scientificName
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
        entryTimestamp.onNext(DateTime.now())

    }

    fun setSpecies(speciesName: String) {
        entrySpeciesName.onNext(speciesName)
    }

    fun setEntryRarity(rarity: ObservationRarity) {
        this.rarity = rarity
    }

    fun setEntryDescription(desc: String) {
        entryDescription.onNext(desc)
    }

    override fun onCleared() {
        job.cancelChildren()
        super.onCleared()
    }

}
