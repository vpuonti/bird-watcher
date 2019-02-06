package fi.valtteri.birdwatcher.data.observations

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fi.valtteri.birdwatcher.data.entities.Observation
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface ObservationDao {

    @Query("SELECT * FROM observations")
    fun getAllObservations() : Flowable<List<Observation>>

    @Insert
    fun insertObservation(vararg observation: Observation) : Completable

    @Delete
    fun deleteObservation(vararg observation: Observation)

    @Query("DELETE FROM observations")
    fun deleteAll()
}