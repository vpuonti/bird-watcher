package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.net.Uri
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
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import org.joda.time.DateTime
import timber.log.Timber
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

    private val entryTimestamp: BehaviorSubject<DateTime> = BehaviorSubject.create()
    private val entrySpeciesName: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryDescription: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entryPicFileName: Observable<String> = entryTimestamp.map { "${it.millis}_bird" }
    private val entryPicUri: BehaviorSubject<Uri> = BehaviorSubject.create()
    private val entryLatitude: BehaviorSubject<Double> = BehaviorSubject.create()
    private val entryLongitude: BehaviorSubject<Double> = BehaviorSubject.create()
    private val entryRarity: BehaviorSubject<ObservationRarity> = BehaviorSubject.create()

    private val takeIntoEvaluation :List<BehaviorSubject<out Any>> = listOf(entrySpeciesName, entryDescription, entryLongitude, entryLatitude)

    private val saveAllowed =
        Observable.combineLatest(takeIntoEvaluation) {args ->
            val name: String = args[0] as String
            val desc: String = args[1] as String
            val lat: Double = args[2] as Double
            val lng: Double = args[3] as Double
            Timber.d("Name: $name\n Desc: $desc\n LatLng: $lat -- $lng")
            return@combineLatest (name.isNotBlank() && desc.isNotBlank())

        }
        .doOnNext { Timber.d("All gucci: $it") }

    init {
        val speciesDisposable = speciesRepository.getSpecies().subscribe { it -> speciesData.postValue(it) }

        compositeDisposable.addAll(speciesDisposable)
    }

    fun getSpecies(): LiveData<List<Species>> {
        return speciesData
    }

    fun getSpeciesNames() : LiveData<List<String>> = LiveDataReactiveStreams.fromPublisher(speciesRepository.getSpeciesNames())

    // getter for entry livedata
    fun getEntryTimestamp() = LiveDataReactiveStreams.fromPublisher(entryTimestamp.toFlowable(BackpressureStrategy.LATEST))
    fun getEntrySpeciesName() = LiveDataReactiveStreams.fromPublisher(entrySpeciesName.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryDescription() = LiveDataReactiveStreams.fromPublisher(entryDescription.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryPicFileName() = LiveDataReactiveStreams.fromPublisher(entryPicFileName.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryPicUri() = LiveDataReactiveStreams.fromPublisher(entryPicUri.toFlowable(BackpressureStrategy.LATEST))
    fun isSaveAllowed() = LiveDataReactiveStreams.fromPublisher(saveAllowed.toFlowable(BackpressureStrategy.LATEST))

    fun initializeNewEntry(){
        entryTimestamp.onNext(DateTime.now())

    }

    fun setSpecies(speciesName: String) {
        entrySpeciesName.onNext(speciesName)
    }

    fun setEntryRarity(rarity: ObservationRarity) {
        entryRarity.onNext(rarity)
    }

    fun setEntryDescription(desc: String) {
        entryDescription.onNext(desc)
    }

    fun setPictureUri(uri: Uri) {
        entryPicUri.onNext(uri)
    }

    fun setEntryLatLng(lat: Double, lng: Double) {
        entryLatitude.onNext(lat)
        entryLongitude.onNext(lng)
    }

    override fun onCleared() {
        job.cancelChildren()
        compositeDisposable.dispose()
        super.onCleared()
    }

}
