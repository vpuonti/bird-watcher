package fi.valtteri.birdwatcher.data.species

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Flowables

@Dao
interface SpeciesDao {

    @Insert
    fun insert(vararg species: Species)

    @Query("SELECT * FROM species")
    fun getSpecies() : Flowable<List<Species>>

    @Query("DELETE FROM species")
    fun deleteAll()

    @Query("SELECT * FROM species WHERE scientificName = (:sciName) LIMIT 1")
    fun getSpecimenWithScientificName(sciName: String) : Species?

    @Transaction
    fun updateData(newData: List<Species>) {
        val new = Flowable.just(newData)
        Flowables.combineLatest(new, getSpecies()) {newSpecies, oldSpecies ->
            newSpecies.filter {
                return@filter getSpecimenWithScientificName(it.scientificName) == null
            }
        }



    }

}