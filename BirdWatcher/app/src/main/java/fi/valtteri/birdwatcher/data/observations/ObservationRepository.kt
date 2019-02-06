package fi.valtteri.birdwatcher.data.observations

import android.net.Uri
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import fi.valtteri.birdwatcher.data.entities.Species
import fi.valtteri.birdwatcher.data.species.SpeciesDao
import io.reactivex.Completable
import org.joda.time.DateTime
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationRepository @Inject constructor(
    private val speciesDao: SpeciesDao,
    private val observationDao: ObservationDao
) {

    private var species: List<Species> = emptyList()

    init {
        speciesDao.getSpecies()
            .doOnNext { species = it }
            .subscribe()
    }

    fun saveObservation(observation: Observation) : Completable {
        return Completable.create { emitter ->
            observationDao.insertObservation(observation).subscribe(
                { emitter.onComplete()},
                { emitter.onError(it)}) }

    }

    fun getObservations() = observationDao.getAllObservations()

}