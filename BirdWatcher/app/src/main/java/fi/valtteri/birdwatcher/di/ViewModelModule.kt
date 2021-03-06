package fi.valtteri.birdwatcher.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fi.valtteri.birdwatcher.ui.settings.SettingsViewModel
import fi.valtteri.birdwatcher.ui.addentry.AddEntryViewModel
import fi.valtteri.birdwatcher.ui.main.MainViewModel
import fi.valtteri.birdwatcher.ui.observations.ObservationsViewModel
import javax.inject.Singleton

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ObservationsViewModel::class)
    abstract fun bindObservationsViewModel(observationsViewModel: ObservationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddEntryViewModel::class)
    abstract fun bindAddEntryViewModel(addEntryViewModel: AddEntryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel) : ViewModel

    @Binds
    @Singleton
    abstract fun bindViewModelFactory(factory: BirdWatcherViewModelFactory): ViewModelProvider.Factory

}
