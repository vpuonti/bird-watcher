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

    fun saveObservation(
        speciesName: String,
        timeStamp: DateTime,
        rarity: ObservationRarity,
        latitude: Double,
        longitude: Double,
        picUri: Uri? = null
    ) : Completable {
        return Completable.create { emitter ->

            Timber.d("HAHAHHHHHHEQWEQ")

            val theBird = species.firstOrNull { bird -> (
                    bird.scientificName == speciesName ||
                            bird.finnishName == speciesName ||
                            bird.englishName == speciesName ||
                            bird.swedishName == speciesName)
            }
            Timber.d("The bird $theBird")
            emitter.onComplete()
//            if (theBird == null) {
//                emitter.onError(IllegalArgumentException("There isn't a bird named $speciesName in DB..."))
//            } else {
//                val observation = Observation(
//                    speciesId = theBird.id,
//                    timeStamp = timeStamp,
//                    rarity = rarity,
//                    latitude = latitude,
//                    longitude = longitude,
//                    picUri = picUri
//                )
//                observationDao.insertObservation(observation)
//                    .subscribe ({emitter.onComplete()}, {emitter.onError(it)})
//            }



        }

    }

}