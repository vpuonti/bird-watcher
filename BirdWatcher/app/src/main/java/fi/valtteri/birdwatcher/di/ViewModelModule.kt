package fi.valtteri.birdwatcher.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fi.valtteri.birdwatcher.ui.observations.ObservationsViewModel

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: BirdWatcherViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(ObservationsViewModel::class)
    abstract fun bindObservationsViewModel(observationsViewModel: ObservationsViewModel): ViewModel

}