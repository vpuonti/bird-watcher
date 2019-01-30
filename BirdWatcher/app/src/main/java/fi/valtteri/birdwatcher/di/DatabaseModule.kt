package fi.valtteri.birdwatcher.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fi.valtteri.birdwatcher.data.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Module
class DatabaseModule @Inject constructor(private val appContext: Context) {

    @Provides
    @Singleton
    fun database(): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "bird_watcher_db").build()
    }

}