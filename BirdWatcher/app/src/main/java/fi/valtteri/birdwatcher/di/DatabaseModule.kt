package fi.valtteri.birdwatcher.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class DatabaseModule @Inject constructor(private val appContext: Context) {

    @Provides
    @Singleton
    fun database():

}