package fi.valtteri.birdwatcher.ui.observations

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel;
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.observations.ObservationRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import java.lang.IllegalArgumentException
import javax.inject.Inject

class ObservationsViewModel @Inject constructor(
    observationsRepository: ObservationRepository
): ViewModel() {

    private val sortingModeSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(Observation.SORT_TIMESTAMP_DESCENDING)

    private val observations : Flowable<List<Observation>> =  Flowables.combineLatest(
        observationsRepository.getObservations(),
        sortingModeSubject.toFlowable(BackpressureStrategy.LATEST)) { observations, sortingMode ->
        when(sortingMode) {
            Observation.SORT_TIMESTAMP_ASCENDING -> {
                 return@combineLatest observations.sortedBy { it.timeStamp.millis }
            }
            Observation.SORT_TIMESTAMP_DESCENDING -> {
                val millisNow = DateTime.now().millis
                return@combineLatest observations.sortedBy { millisNow - it.timeStamp.millis }
            }
            else -> {
                return@combineLatest observations
            }
        }
    }

    fun getObservations() = LiveDataReactiveStreams.fromPublisher(observations)


    fun setSortingMode(sortingMode: Int) {
        when(sortingMode) {
            Observation.SORT_TIMESTAMP_DESCENDING -> {
                sortingModeSubject.onNext(sortingMode)
            }
            Observation.SORT_TIMESTAMP_ASCENDING -> {
                sortingModeSubject.onNext(sortingMode)
            }
            else -> {
                throw IllegalArgumentException("Sorting mode not found.")
            }
        }
    }



}
