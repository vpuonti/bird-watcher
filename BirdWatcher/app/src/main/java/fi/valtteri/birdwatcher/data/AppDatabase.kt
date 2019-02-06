package fi.valtteri.birdwatcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fi.valtteri.birdwatcher.data.converters.DateTimeTypeConverter
import fi.valtteri.birdwatcher.data.converters.ObservationRarityTypeConverter
import fi.valtteri.birdwatcher.data.converters.UriTypeConverter
import fi.valtteri.birdwatcher.data.entities.Observation
import fi.valtteri.birdwatcher.data.entities.Species
import fi.valtteri.birdwatcher.data.observations.ObservationDao
import fi.valtteri.birdwatcher.data.species.SpeciesDao

@Database(entities = [Species::class, Observation::class], version = 1
    , exportSchema = false)
@TypeConverters(ObservationRarityTypeConverter::class, DateTimeTypeConverter::class, UriTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun speciesDao(): SpeciesDao

    abstract fun observationDao() : ObservationDao



}