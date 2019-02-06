package fi.valtteri.birdwatcher.data.converters

import androidx.room.TypeConverter
import org.joda.time.DateTime

class DateTimeTypeConverter {

    @TypeConverter
    fun toLong(dateTime: DateTime) : Long {
        return dateTime.millis
    }

    @TypeConverter
    fun toDateTime(millis: Long) : DateTime {
        return DateTime(millis)
    }

}