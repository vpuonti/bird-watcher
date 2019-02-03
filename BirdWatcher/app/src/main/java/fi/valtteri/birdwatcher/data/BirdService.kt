package fi.valtteri.birdwatcher.data

import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Observable
import retrofit2.http.GET

interface BirdService {

    @GET("species")
    fun getSpecies() : Observable<List<Species>>

}