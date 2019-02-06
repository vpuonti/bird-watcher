package fi.valtteri.birdwatcher.data.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(tableName = "observations")
data class Observation (
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,
    val speciesId: Int,
    val timeStamp: DateTime,
    val description: String,
    val rarity: ObservationRarity,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val picUri: Uri? = null
)

