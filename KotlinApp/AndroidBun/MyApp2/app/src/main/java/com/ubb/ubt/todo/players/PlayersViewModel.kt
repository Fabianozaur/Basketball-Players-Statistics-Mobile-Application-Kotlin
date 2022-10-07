package com.ubb.ubt.todo.players

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

class PlayersListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val players: LiveData<List<Player>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val playerRepository: PlayerRepository

    init {
        val playerDao = PlayerDatabase.getDatabase(application, viewModelScope).playerDao()
        playerRepository = PlayerRepository(playerDao)
        players = playerRepository.players
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...")
            mutableLoading.value = true
            mutableException.value = null
            when (val result = playerRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded")
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception)
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }

    fun syncDataWithServer() {
        viewModelScope.launch {
            Log.v(TAG, "sending local changes to the server...")
            mutableLoading.value = true
            mutableException.value = null
            playerRepository.syncData()
            //playerRepository.startSyncJob()
            mutableLoading.value = false
        }
    }

}