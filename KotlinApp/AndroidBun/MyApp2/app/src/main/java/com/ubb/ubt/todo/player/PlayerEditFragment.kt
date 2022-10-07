package com.ubb.ubt.todo.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ubb.ubt.core.TAG
import com.ubb.ubt.R
import com.ubb.ubt.databinding.FragmentPlayerEditBinding
import com.ubb.ubt.todo.data.Player

class PlayerEditFragment : Fragment() {
    companion object {
        const val PLAYER_ID = "PLAYER_ID"
    }

    private lateinit var viewModel: PlayerEditViewModel
    private var playerId: String? = null
    private var player: Player? = null

    private var _binding: FragmentPlayerEditBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")
        arguments?.let {
            if (it.containsKey(PLAYER_ID)) {
                playerId = it.getString(PLAYER_ID).toString()
            }
        }
        _binding = FragmentPlayerEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated")
        setupViewModel()
        binding.fab.setOnClickListener {
            Log.v(TAG, "save player")
            val i = player
            if (i != null) {
                i.name = binding.playerName.text.toString()
                i.number = binding.playerNumber.text.toString().toInt()
                i.ppg = binding.playerPpg.text.toString().toInt()
                i.roman = binding.playerRoman.isChecked
                viewModel.saveOrUpdatePlayer(i)
            }
        }
        binding.playerName.setText(playerId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.i(TAG, "onDestroyView")
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(PlayerEditViewModel::class.java)
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            binding.progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().navigate(R.id.action_PlayerEditFragment_to_PlayerListFragment)
            }
        })
        val id = playerId
        if (id == null) {
            player = Player("", "", 0, 0, false)
        } else {
            viewModel.getPlayerById(id).observe(viewLifecycleOwner, {
                Log.v(TAG, "update players")
                if (it != null) {
                    player = it
                    binding.playerName.setText(it.name)
                    binding.playerNumber.setText(it.number.toString())
                    binding.playerPpg.setText(it.ppg.toString())
                    binding.playerRoman.isChecked = it.roman
                }
            })
        }
    }

}