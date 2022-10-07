package com.ubb.ubt.todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ubb.ubt.todo.data.Player

@Dao
interface PlayerDao {
    @Query("SELECT * from players ORDER BY ppg DESC")
    fun getAll(): LiveData<List<Player>>

    @Query("SELECT * FROM players WHERE _id=:id ")
    fun getById(id: String): LiveData<Player>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(players: Player)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(player: Player)

    @Query("DELETE FROM players")
    suspend fun deleteAll()
}