package fi.valtteri.birdwatcher.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "species")
data class Species (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val abbreviation: String,
    val scienticName: String,
    val finnishName: String,
    val swedishName: String,
    val englishName: String
)