package fi.valtteri.birdwatcher.data

import android.content.Context
import android.content.SharedPreferences
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.CompletableOnSubscribe
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Seconds
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeciesRepository @Inject constructor(
    private val speciesDao: SpeciesDao,
    private val birdService: BirdService,
    private val sharedPreferences: SharedPreferences,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {


    fun getSpecies() : Flowable<List<Species>> {

        if (!isFresh()) {
            Timber.d("Data not fresh")
            fetchNewSpeciesDataAndUpdateDb()
                .subscribeOn(Schedulers.io())
                .doOnComplete { Timber.d("Updated db data") }
                .doOnError { Timber.e("Error updating db data $it")}
                .subscribe()

        } else {
            Timber.d("Data is fresh")
        }
        return speciesDao.getSpecies()
            .filter { it.isNotEmpty() }
    }

    /**
     * If over 12 hours since last update (can be set to whatever)
     */
    private fun isFresh(): Boolean {
        var lastUpdated : DateTime? = null
        try {
            lastUpdated = DateTime.parse(sharedPreferences.getString(SPECIES_FETCHED, null))
            val secondsBetween = Seconds.secondsBetween(lastUpdated ,DateTime.now()).seconds
            Timber.d("Seconds between updates: $secondsBetween")
        } catch (e: Exception) {}
        return !(lastUpdated == null || Hours.hoursBetween(lastUpdated, DateTime.now()).hours > 12)
    }

    fun getSpeciesNames(): Flowable<List<String>> =
        settingsRepository.getLanguagePref().switchMap { mapSpeciesToNames(it) }
            .doOnNext { Timber.d("Names: $it") }


    private fun fetchNewSpeciesDataAndUpdateDb() : Completable {
        return Completable.create {completableEmitter ->
            birdService.getSpecies()
                .subscribeOn(Schedulers.io())
                .subscribe (
                    {
                        speciesDao.updateData(it)
                        sharedPreferences.edit().putString(SPECIES_FETCHED, DateTime.now().toString()).apply()
                        completableEmitter.onComplete()
                    },
                    {
                        Timber.e("Bird service error: $it")
                        completableEmitter.onError(it)
                    })

        }
    }

    /**
     * If language: scientific -> map List<Species> to List<String> where String = Species.scientificName
     * Same for all configured languages. (Configured in res/values/strings as <string-array>)
     */
    private fun mapSpeciesToNames(language: String): Flowable<List<String>> {
        val languages = context.resources.getStringArray(R.array.species_language_choices)
        return getSpecies().map { it ->
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




    companion object {
        private const val SPECIES_FETCHED = "species_fetched"
    }

}