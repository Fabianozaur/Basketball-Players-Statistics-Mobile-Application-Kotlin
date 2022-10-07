package com.ubb.ubt.todo.data

import androidx.lifecycle.LiveData
import androidx.work.*
import com.ubb.ubt.core.Result
import com.ubb.ubt.todo.data.local.PlayerDao
import com.ubb.ubt.todo.data.remote.PlayerApi
import com.ubb.ubt.todo.players.SimpleWorker

class PlayerRepository(private val playerDao: PlayerDao) {

    val players = playerDao.getAll()

    suspend fun refresh(): Result<Boolean> {
        try {
            val players = PlayerApi.service.find()
            for (player in players) {
                playerDao.insert(player)
            }
            return Result.Success(true)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    fun getById(playerId: String): LiveData<Player> {
        return playerDao.getById(playerId)
    }

    suspend fun save(player: Player): Result<Player> {
        try {
            val createdPlayer = PlayerApi.service.create(player)
            playerDao.insert(createdPlayer)
            return Result.Success(createdPlayer)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(player: Player): Result<Player> {
        try {
            val updatedPlayer = PlayerApi.service.update(player._id, player)
            return Result.Success(updatedPlayer)
        }
        catch(e: Exception) {
            try {
                playerDao.update(player)
                return Result.Success(player)
            } catch(e: Exception) {
                return Result.Error(e)
            }
        }
    }

    fun startSyncJob() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .put("1", players.value)
            .build()
//        val myWork = PeriodicWorkRequestBuilder<ExampleWorker>(1, TimeUnit.MINUTES)
        val myWork = OneTimeWorkRequest.Builder(SimpleWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workId = myWork.id
        WorkManager.getInstance().apply {
            // enqueue Work
            enqueue(myWork)
            // observe work status
            getWorkInfoByIdLiveData(workId)
        }
    }

    suspend fun syncData(){
        players.value?.forEach { player ->
            PlayerApi.service.update(player._id, player)
        }
    }
}