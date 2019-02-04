package fi.valtteri.birdwatcher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.android.material.bottomappbar.BottomAppBar
import fi.valtteri.birdwatcher.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
        val context: Context,
        val sharedPreferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener
{

    private val currentlySelectedLanguagePreference: BehaviorSubject<String> = BehaviorSubject.create()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun setSpeciesLanguagePref(preference: String) {
        val choices = context.resources.getStringArray(R.array.species_language_choices)
        if(choices.indexOf(preference) == -1) {
            throw IllegalArgumentException("$preference not in choices. Valid choices: $choices")
        }
        Timber.d("Setting language pref to: $preference ")
        sharedPreferences.edit().putString(SPECIES_LANGUAGE_PREFERENCE, preference).apply()
    }

    fun getLanguagePref() : Flowable<String> = currentlySelectedLanguagePreference.toFlowable(BackpressureStrategy.LATEST)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key!!) {
            SPECIES_LANGUAGE_PREFERENCE -> {
                Timber.d("$SPECIES_LANGUAGE_PREFERENCE changed!")
                sharedPreferences?.let { sp ->
                    val str = sp.getString(SPECIES_LANGUAGE_PREFERENCE, null)
                    str?.let { currentlySelectedLanguagePreference.onNext(it) }
                }
            }
        }
    }

    companion object {
        private const val SPECIES_LANGUAGE_PREFERENCE = "SPECIES_LANGUAGE_PREFERENCE"
    }
}