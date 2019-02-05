package fi.valtteri.birdwatcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.Species

@Database(entities = [Species::class, Observation::class], version = 4
    , exportSchema = false)
@TypeConverters(ObservationRarityTypeConverter::class, DateTimeTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun speciesDao(): SpeciesDao

    abstract fun observationDao() : ObservationDao



}