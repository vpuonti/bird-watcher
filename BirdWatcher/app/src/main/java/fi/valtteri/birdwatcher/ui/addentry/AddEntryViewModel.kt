package fi.valtteri.birdwatcher.ui.addentry

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import fi.valtteri.birdwatcher.data.observations.ObservationRepository
import fi.valtteri.birdwatcher.data.species.SpeciesRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject

class AddEntryViewModel @Inject constructor(
    private val speciesRepository: SpeciesRepository,
    private val observationRepository: ObservationRepository,
    private val context: Context
) : ViewModel() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    // data to make observation
    private val entryTimestamp: BehaviorSubject<Optional<DateTime>> = BehaviorSubject.createDefault(Optional.empty())
    private val entrySpecies: BehaviorSubject<Optional<Species>> = BehaviorSubject.createDefault(Optional.empty())
    private val entryDescription: BehaviorSubject<Optional<String>> = BehaviorSubject.createDefault(Optional.empty())


    private val entryPicFileName: Observable<Optional<out String>> = entryTimestamp.map { it ->
        if(it.isPresent) {
            return@map Optional.of("${it.get().millis}_bird")
        } else {
            return@map Optional.ofNullable(null)
        }
    }
    private val entryPicUri: BehaviorSubject<Optional<Uri>> = BehaviorSubject.createDefault(Optional.empty())
    private val entryLatitude: BehaviorSubject<Optional<Double>> = BehaviorSubject.createDefault(Optional.empty())
    private val entryLongitude: BehaviorSubject<Optional<Double>> = BehaviorSubject.createDefault(Optional.empty())
    private val entryRarity: BehaviorSubject<Optional<ObservationRarity>> = BehaviorSubject.createDefault(Optional.empty())

    private val observationFromUi: Observable<Optional<Observation>> =
        Observables.combineLatest(
            entryDescription,
            entryTimestamp,
            entryRarity,
            entrySpecies,
            entryLatitude,
            entryLongitude,
            entryPicUri
            ) {
                descOptional,
                timeStampOptional,
                rarityOptional,
                speciesOptional,
                latitudeOptional,
                longitudeOptional,
                picUriOptional ->


        if(timeStampOptional.isPresent && descOptional.isPresent && descOptional.get().isNotBlank() &&
                speciesOptional.isPresent && rarityOptional.isPresent) {
            val speciesId = speciesOptional.get().id
            val timestamp = timeStampOptional.get()
            val desc = descOptional.get()
            val rarity = rarityOptional.get()

            return@combineLatest Optional.of(
                Observation(
                    speciesId = speciesId,
                    timeStamp = timestamp,
                    description = desc,
                    rarity = rarity,
                    latitude = latitudeOptional.orElse(null),
                    longitude = longitudeOptional.orElse(null),
                    picUri = picUriOptional.orElse(null)
                )
            )
        }

        return@combineLatest Optional.empty<Observation>()

    }

    private val latestObservationData: BehaviorSubject<Optional<Observation>> = BehaviorSubject.createDefault(Optional.empty())

    private val saveAllowed =
        Observables.combineLatest(entrySpecies, entryDescription) { speciesOptional, descriptionOptional ->
            if(speciesOptional.isPresent && descriptionOptional.isPresent) {
                val desc = descriptionOptional.get()
                return@combineLatest (desc.isNotBlank())
            } else {
                return@combineLatest false
            }

        }




    init {
        observationFromUi.subscribe(latestObservationData)
    }


    fun getSpecies(): LiveData<List<Species>> = LiveDataReactiveStreams.fromPublisher(speciesRepository.getSpecies())
    fun getEntryTimestamp() = LiveDataReactiveStreams.fromPublisher(entryTimestamp.filter{it.isPresent}.map { it.get() }.toFlowable(BackpressureStrategy.LATEST))
    fun getEntrySpecies() = LiveDataReactiveStreams.fromPublisher(entrySpecies.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryDescription() = LiveDataReactiveStreams.fromPublisher(entryDescription.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryPicFileName() = LiveDataReactiveStreams.fromPublisher(entryPicFileName.filter{it.isPresent}.map { it.get() }.toFlowable(BackpressureStrategy.LATEST))
    fun getEntryPicUri() = LiveDataReactiveStreams.fromPublisher(entryPicUri.filter{it.isPresent}.map { it.get() }.toFlowable(BackpressureStrategy.LATEST))
    fun isSaveAllowed() = LiveDataReactiveStreams.fromPublisher(saveAllowed.toFlowable(BackpressureStrategy.LATEST))

    fun initializeNewEntry(){
        entryTimestamp.onNext(Optional.of(DateTime.now()))

    }

    fun setEntrySpecies(species: Species) {
        entrySpecies.onNext(Optional.of(species))
    }

    fun setEntryRarity(rarity: ObservationRarity) {
        entryRarity.onNext(Optional.of(rarity))
    }

    fun setEntryDescription(desc: String) {
        if(desc.isBlank()) {
            entryDescription.onNext(Optional.empty())
        } else {
            entryDescription.onNext(Optional.of(desc))

        }
    }

    fun setPictureUri(uri: Uri) {
        entryPicUri.onNext(Optional.of(uri))
    }

    fun setEntryLatLng(lat: Double, lng: Double) {
        entryLatitude.onNext(Optional.of(lat))
        entryLongitude.onNext(Optional.of(lng))
    }

    fun saveObservation(): Completable {
        return Completable.create { emitter ->
            if (!latestObservationData.hasValue()) {
                emitter.onError(RuntimeException("Viewmodel doesn't have latest data from UI to create Observation (???)"))
            } else {
                val latestDataObservable = latestObservationData.value!!
                if (!latestDataObservable.isPresent) {
                    emitter.onError(RuntimeException("Viewmodel doesn't have latest data from UI to create Observation (???)"))
                } else {
                    val latestData = latestDataObservable.get()
                    Timber.d("Saving $latestData")
                    scope.launch(Dispatchers.IO) {
                        observationRepository.saveObservation(latestData)
                            .subscribe(
                                {Timber.d("Saved")
                                    emitter.onComplete()
                                }, { emitter.onError(it) })
                    }
                    }


            }
        }
    }

    override fun onCleared() {
        job.cancelChildren()
        compositeDisposable.dispose()
        super.onCleared()
    }

}
