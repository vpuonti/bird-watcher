package fi.valtteri.birdwatcher.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fi.valtteri.birdwatcher.data.AppDatabase
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun database(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "bird_watcher_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun speciesDao(db: AppDatabase) = db.speciesDao()

    @Provides
    @Singleton
    fun observationDao(db: AppDatabase) = db.observationDao()




}