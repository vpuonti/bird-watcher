package fi.valtteri.birdwatcher.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fi.valtteri.birdwatcher.MainActivity
import fi.valtteri.birdwatcher.ui.addentry.AddEntryActivity

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributesMainActivity() : MainActivity

    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributesAddEntryActivity() : AddEntryActivity


}