package fi.valtteri.birdwatcher.data

import androidx.room.TypeConverter
import fi.valtteri.birdwatcher.data.entities.ObservationRarity


class ObservationRarityTypeConverter {

    @TypeConverter
    fun toString(observationRarity: ObservationRarity) : Int {
        return observationRarity.ordinal
    }

    @TypeConverter
    fun toObservationRarity(int: Int) : ObservationRarity {
        return ObservationRarity.values()[int]
    }

}