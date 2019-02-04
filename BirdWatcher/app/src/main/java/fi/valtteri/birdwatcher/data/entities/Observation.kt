package fi.valtteri.birdwatcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(tableName = "observations")
data class Observation (
    @PrimaryKey (autoGenerate = true)
    val id: Int,
    val speciesId: Int,
    val timeStamp: DateTime,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rarity: ObservationRarity,
    val picName: String? = null
)

