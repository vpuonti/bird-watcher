package fi.valtteri.birdwatcher.data.converters

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun toString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun toUri(str: String): Uri {
        return Uri.parse(str)
    }
}