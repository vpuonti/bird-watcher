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
        private val context: Context,
        private val sharedPreferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener
{

    private val currentlySelectedLanguagePreference: BehaviorSubject<String> = BehaviorSubject.create()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val selectedLang = sharedPreferences.getString(SPECIES_LANGUAGE_PREFERENCE, null)
        Timber.d("INIT SETTINGS REPO. Default lang pref: $selectedLang")
        selectedLang?.let {
            currentlySelectedLanguagePreference.onNext(it)
        }
        //set initial value if none set
        if(selectedLang == null) {
            val defaultValue = context.resources.getStringArray(R.array.species_language_choices)[0]
            sharedPreferences.edit().putString(SPECIES_LANGUAGE_PREFERENCE, defaultValue).apply()
        }
    }


    fun setSpeciesLanguagePref(preference: String) {
        val choices = context.resources.getStringArray(R.array.species_language_choices)
        if(choices.indexOf(preference) == -1) {
            throw IllegalArgumentException("$preference not in choices. Valid choices: $choices")
        }
        Timber.d("Setting language pref to: $preference ")
        sharedPreferences.edit().putString(SPECIES_LANGUAGE_PREFERENCE, preference).apply()
    }

    fun getLanguagePref() : Flowable<String> = currentlySelectedLanguagePreference.toFlowable(BackpressureStrategy.BUFFER)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Timber.d("Shared prefs changed. Key: $key")
        when (key!!) {
            SPECIES_LANGUAGE_PREFERENCE -> {
                Timber.d("$SPECIES_LANGUAGE_PREFERENCE changed!")
                sharedPreferences?.let { sp ->
                    val str = sp.getString(SPECIES_LANGUAGE_PREFERENCE, null)
                    str?.let {
                       Timber.d("Setting $it to behaviorsubject")
                        currentlySelectedLanguagePreference.onNext(it)
                    }
                }
            }
        }
    }

    companion object {
        private const val SPECIES_LANGUAGE_PREFERENCE = "SPECIES_LANGUAGE_PREFERENCE"
    }
}