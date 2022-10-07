package com.ubb.ubt.todo.data.remote

import com.ubb.ubt.core.Api
import com.ubb.ubt.todo.data.Player
import retrofit2.http.*

object PlayerApi {
    interface Service {
        @GET("/api/player")
        suspend fun find(): List<Player>

        @GET("/api/player/{id}")
        suspend fun read(@Path("id") playerId: String): Player;

        @Headers("Content-Type: application/json")
        @POST("/api/player")
        suspend fun create(@Body player: Player): Player

        @Headers("Content-Type: application/json")
        @PUT("/api/player/{id}")
        suspend fun update(@Path("id") playerId: String, @Body player: Player): Player
    }

    val service: Service = Api.retrofit.create(Service::class.java)
}