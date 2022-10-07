package com.ubb.ubt.todo.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ubb.ubt.core.Result
import com.ubb.ubt.core.TAG
import com.ubb.ubt.todo.data.Player
import com.ubb.ubt.todo.data.PlayerRepository
import com.ubb.ubt.todo.data.local.PlayerDatabase
import kotlinx.coroutines.launch

class PlayerEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val playerRepository: PlayerRepository

    init {
        val playerDao = PlayerDatabase.getDatabase(application, viewModelScope).playerDao()
        playerRepository = PlayerRepository(playerDao)
    }

    fun getPlayerById(playerId: String): LiveData<Player> {
        Log.v(TAG, "getPlayerById...")
        return playerRepository.getById(playerId)
    }

    fun saveOrUpdatePlayer(player: Player) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdatePlayer...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Player>
            if (player._id.isNotEmpty()) {
                result = playerRepository.update(player)
            } else {
                result = playerRepository.save(player)
            }
            when(result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdatePlayer succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdatePlayer failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }
}