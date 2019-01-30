package fi.valtteri.birdwatcher.data.entities

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Observation (
    @PrimaryKey
    val id: UUID,
    val location: Location
    )

