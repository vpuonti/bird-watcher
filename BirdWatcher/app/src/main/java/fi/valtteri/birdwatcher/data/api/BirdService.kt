package fi.valtteri.birdwatcher.data.api

import fi.valtteri.birdwatcher.data.entities.Species
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET

interface BirdService {

    @GET("/species")
    fun getSpecies() : Single<List<Species>>

}