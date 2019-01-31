package fi.valtteri.birdwatcher.di

import androidx.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fi.valtteri.birdwatcher.MainActivity
import javax.inject.Singleton

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributesMainActivity() : MainActivity


}