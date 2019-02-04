package fi.valtteri.birdwatcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable

@Dao
interface SpeciesDao {

    @Insert
    fun insert(vararg species: Species)

    @Query("SELECT * FROM species")
    fun getSpecies() : Flowable<List<Species>>

    @Query("DELETE FROM species")
    fun deleteAll()

}