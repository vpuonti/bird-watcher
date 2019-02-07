package fi.valtteri.birdwatcher.ui.observations

import android.net.Uri
import fi.valtteri.birdwatcher.data.entities.ObservationRarity
import org.joda.time.DateTime

data class ObservationCardData (
    val speciesDisplayName: String?,
    val rarity: ObservationRarity,
    val notes: String,
    val timeStamp: DateTime,
    val pictureUri: Uri?,
    val latitude: Double?,
    val longitude: Double?
    )