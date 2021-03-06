package fi.valtteri.birdwatcher.data.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonQualifier

@Entity(tableName = "species")
data class Species (
    @PrimaryKey
    val id: Int,
    @Json(name = "speciesAbbr")
    val abbreviation: String,
    @Json(name = "speciesSCI")
    val scientificName: String,
    @Json(name = "speciesFI")
    val finnishName: String,
    @Json(name = "speciesSV")
    val swedishName: String,
    @Json(name = "speciesEN")
    val englishName: String
) {
    @Ignore
    var displayName: String? = null
}