package fi.valtteri.birdwatcher.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fi.valtteri.birdwatcher.ui.addentry.AddEntryFragment
import fi.valtteri.birdwatcher.ui.observations.ObservationsFragment

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributesObservationsFragment(): ObservationsFragment

    @ContributesAndroidInjector
    abstract fun contributesAddEntryFragment(): AddEntryFragment
}