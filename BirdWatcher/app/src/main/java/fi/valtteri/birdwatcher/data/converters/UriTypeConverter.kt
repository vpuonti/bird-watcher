package fi.valtteri.birdwatcher.data.converters

import android.net.Uri
import androidx.room.TypeConverter

class UriTypeConverter {
    @TypeConverter
    fun toString(uri: Uri?): String? {
        if (uri == null) {
            return null
        } else {
            return uri.toString()
        }
    }

    @TypeConverter
    fun toUri(str: String?): Uri? {
        if (str == null) {
            return null
        } else {
            return Uri.parse(str)
        }
    }
}