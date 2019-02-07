package fi.valtteri.birdwatcher.data.species

import android.content.Context
import android.content.SharedPreferences
import fi.valtteri.birdwatcher.R
import fi.valtteri.birdwatcher.data.api.BirdService
import fi.valtteri.birdwatcher.data.entities.Species
import fi.valtteri.birdwatcher.data.settings.SettingsRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Hours
import org.joda.time.Seconds
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeciesRepository @Inject constructor(
    private val speciesDao: SpeciesDao,
    private val birdService: BirdService,
    private val sharedPreferences: SharedPreferences,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {


    private val speciesLastUpdated: BehaviorSubject<DateTime> = BehaviorSubject.create()

    init {
        if(getLastUpdatedFromSharedPrefs() == null) {
            fetchNewSpeciesDataAndUpdateDb()
        }
    }

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
        return Flowables.combineLatest(speciesDao.getSpecies(), settingsRepository.getLanguagePref()) {species, langPref ->
            val languages = context.resources.getStringArray(R.array.species_language_choices)
            return@combineLatest species.map { bird ->
                    when (languages.indexOf(langPref)) {
                        0 -> {
                            bird.displayName = bird.scientificName
                        }
                        1 -> {
                            bird.displayName = bird.finnishName
                        }
                        2 -> {
                            bird.displayName = bird.englishName
                        }
                        3 -> {
                            bird.displayName = bird.swedishName
                        }
                        else -> {
                            throw IllegalArgumentException("Invalid language: $langPref")
                        }
                }
                return@map bird
            }
        }

    }

    private fun isFresh(): Boolean {
        val updatedOn = getLastUpdatedFromSharedPrefs() ?: return false
        val secondsBetween = Seconds.secondsBetween(updatedOn, DateTime.now()).seconds
        Timber.d("Seconds between updates = $secondsBetween")
        return (Hours.hoursBetween(updatedOn, DateTime.now()).hours < 12)
    }



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


    private fun getLastUpdatedFromSharedPrefs():  DateTime? {
        val lastUpdate = sharedPreferences.getString(SPECIES_FETCHED, null)
        if (lastUpdate == null) {
            return null
        } else {
            return DateTime.parse(lastUpdate)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            SPECIES_FETCHED -> {
                getLastUpdatedFromSharedPrefs()?.also { speciesLastUpdated.onNext(it) }
            }
        }
    }




    companion object {
        const val SPECIES_FETCHED = "species_fetched"
    }

}