package fi.valtteri.birdwatcher.data.species

import androidx.room.*
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface SpeciesDao {

    @Insert
    fun insert(vararg species: Species)

    @Query("SELECT * FROM species")
    fun getSpecies() : Flowable<List<Species>>

    @Query("SELECT * FROM species")
    fun getSpeciesSingle(): Single<List<Species>>

    @Query("DELETE FROM species")
    fun deleteAll()

    @Delete
    fun delete(vararg species: Species)

    @Update
    fun update(vararg species: Species)

    @Transaction
    fun updateData(newData: List<Species>) {
        deleteAll()
        insert(*newData.toTypedArray())

    }

}