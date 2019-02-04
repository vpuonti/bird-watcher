package fi.valtteri.birdwatcher.data

import android.content.Context
import android.content.SharedPreferences
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Minutes
import org.joda.time.Seconds
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeciesRepository @Inject constructor(
    var speciesDao: SpeciesDao,
    var birdService: BirdService,
    var sharedPreferences: SharedPreferences,
    val settingsRepository: SettingsRepository,
    val context: Context
) {

    fun getSpecies() : Flowable<List<Species>> {

        if (!isFresh()) {
            Timber.d("Data not fresh... Updating species from API.")
            birdService.getSpecies()
                .subscribeOn(Schedulers.io())
                .subscribe (
                    {
                        speciesDao.deleteAll()
                        speciesDao.insert(*it.toTypedArray())
                    },
                    {Timber.e("Bird service error: ${it}")})
            sharedPreferences.edit().putString(SPECIES_FETCHED, DateTime.now().toString()).apply()

        } else {
            Timber.d("Data is fresh")
        }
        return speciesDao.getSpecies()
            .filter { it.isNotEmpty() }
    }

    private fun isFresh(): Boolean {
        var lastUpdated : DateTime? = null
        try {
            lastUpdated = DateTime.parse(sharedPreferences.getString(SPECIES_FETCHED, null))
            val secondsBetween = Seconds.secondsBetween(lastUpdated ,DateTime.now()).seconds
            Timber.d("Seconds between updates: $secondsBetween")
        } catch (e: Exception) {}

        return !(lastUpdated == null || Seconds.secondsBetween(lastUpdated, DateTime.now()).seconds > 5)
    }



    init {
        GlobalScope.launch {
            speciesDao.deleteAll()
            sharedPreferences.edit().clear().apply()
        }
    }


    companion object {
        private const val SPECIES_FETCHED = "species_fetched"
    }

}