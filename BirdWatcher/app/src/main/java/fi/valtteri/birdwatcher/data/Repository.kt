package fi.valtteri.birdwatcher.data

import android.content.SharedPreferences
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    var speciesDao: SpeciesDao,
    var birdService: BirdService,
    var sharedPreferences: SharedPreferences
) {

    fun getSpecies() : Flowable<List<Species>> {
        //val lastUpdated: String? = sharedPreferences.getString(SPECIES_FETCHED, null)
        val lastUpdated: String? = null
        if (lastUpdated == null) {
            Timber.d("Last updated not found... Updating species from API.")
            birdService.getSpecies()
                .subscribeOn(Schedulers.io())
                .doOnNext { Timber.d("Api returned ${it.size} species") }
                .subscribe ({speciesDao.insert(*it.toTypedArray())}, {Timber.e("Bird service error: ${it}")})
            sharedPreferences.edit().putString(SPECIES_FETCHED, DateTime.now().toString()).apply()

        } else {
            val updatedOn = DateTime.parse(lastUpdated)
            Timber.d("Last updated on $updatedOn")
        }
        return speciesDao.getSpecies()
            .filter { it.isNotEmpty() }
            .doOnNext { Timber.d("Returning ${it.size} species") }
    }





    companion object {
        private const val SPECIES_FETCHED = "species_fetched"
    }

}