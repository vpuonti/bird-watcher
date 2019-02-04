package fi.valtteri.birdwatcher.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel;
import fi.valtteri.birdwatcher.data.SettingsRepository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    var settingsRepository: SettingsRepository
) : ViewModel() {


    fun getCurrentLangSetting() = LiveDataReactiveStreams.fromPublisher(settingsRepository.getLanguagePref())

    fun setCurrentLangSetting(string: String) {
        settingsRepository.setSpeciesLanguagePref(string)
    }


}
