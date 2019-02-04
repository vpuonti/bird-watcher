package fi.valtteri.birdwatcher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.android.material.bottomappbar.BottomAppBar
import fi.valtteri.birdwatcher.R
import io.reactivex.subjects.BehaviorSubject
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
        val context: Context,
        val sharedPreferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener
{

    val currentlySelectedLanguagePreference: BehaviorSubject<String> = BehaviorSubject.create()
    val languageChoices: Array<String> = context.resources.getStringArray(R.array.species_language_choices)

    fun setSpeciesLanguagePref(preference: String) {
        val choices = context.resources.getStringArray(R.array.species_language_choices)
        if(choices.indexOf(preference) == -1) {
            throw IllegalArgumentException("$preference not in choices. Valid choices: $choices")
        }
        sharedPreferences.edit().putString(SPECIES_LANGUAGE_PREFERENCE, preference).apply()

    }


    fun getSpeciesLanguagePref() : String {
        val firstChoice = context.resources.getStringArray(R.array.species_language_choices)[0]
        val pref = sharedPreferences.getString(SPECIES_LANGUAGE_PREFERENCE, null)
        return pref ?: synchronized(this){
            sharedPreferences.edit().putString(SPECIES_LANGUAGE_PREFERENCE, firstChoice)
            return firstChoice
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key!!) {
            SPECIES_LANGUAGE_PREFERENCE -> {
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