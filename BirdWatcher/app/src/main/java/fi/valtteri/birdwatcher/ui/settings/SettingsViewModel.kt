package fi.valtteri.birdwatcher.ui.settings

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel;
import fi.valtteri.birdwatcher.data.settings.SettingsRepository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    var settingsRepository: SettingsRepository
) : ViewModel() {


    fun getCurrentLangSetting() = LiveDataReactiveStreams.fromPublisher(settingsRepository.getLanguagePref())

    fun setCurrentLangSetting(string: String) {
        settingsRepository.setSpeciesLanguagePref(string)
    }


}
