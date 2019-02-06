package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.data.settings.SettingsRepository
import fi.valtteri.birdwatcher.data.species.SpeciesRepository
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import fi.valtteri.birdwatcher.data.observations.ObservationRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.DateTimeComparator
import timber.log.Timber
import java.lang.reflect.Type
import java.net.URI
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

class AddEntryViewModel @Inject constructor(
    private val speciesRepository: SpeciesRepository,
    private val observationRepository: ObservationRepository,
    private val context: Context
) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val latestSpecies: BehaviorSubject<List<Species>> = BehaviorSubject.create()
    private val speciesData: MutableLiveData<List<Species>> = MutableLiveData()


    // data to make observation
    private val entryTimestamp: BehaviorSubject<DateTime> = BehaviorSubject.create()
    private val entrySpeciesName: BehaviorSubject<String> = BehaviorSubject.createDefault("")


    private val entryDescription: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val entrySpeciesId: BehaviorSubject<Int> = BehaviorSubject.create()
    private val entryPicFileName: Observable<String> = entryTimestamp.map { "${it.millis}_bird" }
    private val entryPicUri: BehaviorSubject<Uri> = BehaviorSubject.create()
    private val entryLatitude: BehaviorSubject<Double> = BehaviorSubject.create()
    private val entryLongitude: BehaviorSubject<Double> = BehaviorSubject.create()
    private val entryRarity: BehaviorSubject<ObservationRarity> = BehaviorSubject.create()

    private val takeIntoEvaluation :List<BehaviorSubject<out Any>> = listOf(entrySpeciesName, entryDescription, entryLongitude, entryLatitude)

    private val observationDatas: Map<String ,BehaviorSubject<out Any>> = mapOf(
        Pair("name", entrySpeciesName),
        Pair("speciesId", entrySpeciesId),
        Pair("timeStamp", entryTimestamp),
        Pair("description", entryDescription),
        Pair("longitude", entryLongitude),
        Pair("latitude", entryLatitude),
        Pair("rarity", entryRarity),
        Pair("uri", entryPicUri)
    )

    private val saveData = Observable.zip(observationDatas.values) {data ->
        val keys = observationDatas.keys

        val timeStamp: DateTime = data[keys.indexOf("timeStamp")] as DateTime
        val speciesId: Int = data[keys.indexOf("speciesId")] as Int
        val desc: String = data[keys.indexOf("description")] as String
        val longitude: Double = data[keys.indexOf("longitude")] as Double
        val latitude: Double = data[keys.indexOf("latitude")] as Double
        val rarity: ObservationRarity = data[keys.indexOf("rarity")] as ObservationRarity
        val uri: Uri = data[keys.indexOf("uri")] as Uri

        return@zip Observation(
            speciesId = speciesId,
            timeStamp = timeStamp,
            description = desc,
            longitude = longitude,
            latitude = latitude,
            rarity = rarity,
            picUri = uri
        )

    }


    private val saveAllowed =
        Observable.combineLatest(takeIntoEvaluation) {args ->
            val name: String = args[0] as String
            val desc: String = args[1] as String
            val lat: Double = args[2] as Double
            val lng: Double = args[3] as Double
            return@combineLatest (name.isNotBlank() && desc.isNotBlank())

        }

    init {
        val speciesDisposable = speciesRepository.getSpecies().subscribe {
            latestSpecies.onNext(it)
            speciesData.postValue(it)
        }

        val idDisposable = Observable.combineLatest(arrayOf(entrySpeciesName, latestSpecies)) { args ->
            val name = args[0] as String
            val list = args[1] as List<*>
            val species = list.map { it as Species }
            Timber.d("Name: $name... Species: ${species.size}")
            val id: Int? = species.firstOrNull {
                it.scientificName == name ||
                        it.finnishName == name ||
                        it.englishName == name ||
                        it.swedishName == name
            }?.id
            return@combineLatest Optional.ofNullable(id)
        }.subscribe { id ->
            if(id.isPresent){
                entrySpeciesId.onNext(id.get())
            }
        }

        saveData.subscribe { Timber.d("$it") }


        compositeDisposable.addAll(speciesDisposable, idDisposable)
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

    fun saveObservation() {
        //observationDatas.values.filter { it.hasValue() }
    }

    override fun onCleared() {
        job.cancelChildren()
        compositeDisposable.dispose()
        super.onCleared()
    }

}
