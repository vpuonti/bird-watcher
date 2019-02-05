package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.data.SettingsRepository
import fi.valtteri.birdwatcher.data.SpeciesRepository
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import org.joda.time.DateTime
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(
    private val speciesRepository: SpeciesRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val speciesData: MutableLiveData<List<Species>> = MutableLiveData()


    // data to make observation
    //private var entryTimestamp: DateTime? = null
    //private var entryPicFileName: String? = null
    private var rarity: ObservationRarity? = null

    private val entryTimestamp: BehaviorSubject<DateTime> = BehaviorSubject.create()
    private val entrySpeciesName: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryDescription: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryPicFileName: Observable<String> = entryTimestamp.map { "${it.millis}_bird" }


    init {
        val speciesDisposable = speciesRepository.getSpecies().subscribe { it -> speciesData.postValue(it) }
        val fileDisposable = entryPicFileName.subscribe{fileName ->

        }

        compositeDisposable.addAll(speciesDisposable)
    }

    fun getSpecies(): LiveData<List<Species>> {
        return speciesData
    }

    fun getSpeciesNames() : LiveData<List<String>> = LiveDataReactiveStreams.fromPublisher(speciesRepository.getSpeciesNames())

    fun getEntryTimestamp() = LiveDataReactiveStreams.fromPublisher(entryTimestamp.toFlowable(BackpressureStrategy.LATEST))

    fun getEntrySpeciesName() = LiveDataReactiveStreams.fromPublisher(entrySpeciesName.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryDescription() = LiveDataReactiveStreams.fromPublisher(entryDescription.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryPicFileName() = LiveDataReactiveStreams.fromPublisher(entryPicFileName.toFlowable(BackpressureStrategy.LATEST))

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
        compositeDisposable.dispose()
        super.onCleared()
    }

}
