package fi.valtteri.birdwatcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Species (
    @PrimaryKey
    val id: UUID
)