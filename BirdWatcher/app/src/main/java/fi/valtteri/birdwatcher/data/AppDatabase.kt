package fi.valtteri.birdwatcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import fi.valtteri.birdwatcher.data.entities.Species

@Database(entities = [Species::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun speciesDao(): SpeciesDao

}