package com.ubb.ubt.todo.players

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ubb.ubt.R
import com.ubb.ubt.core.TAG
import com.ubb.ubt.todo.data.Player
import com.ubb.ubt.todo.player.PlayerEditFragment

class PlayerListAdapter(
    private val fragment: Fragment,
) : RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {

    var players = emptyList<Player>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onItemClick: View.OnClickListener = View.OnClickListener { view ->
        val player = view.tag as Player
        fragment.findNavController().navigate(R.id.action_PlayerListFragment_to_PlayerEditFragment, Bundle().apply {
            putString(PlayerEditFragment.PLAYER_ID, player._id)
        })
    };

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item, parent, false)
        Log.v(TAG, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v(TAG, "onBindViewHolder $position")
        val player = players[position]
        holder.textView.text = player.toString()
        holder.itemView.tag = player
        holder.itemView.setOnClickListener(onItemClick)
    }

    override fun getItemCount() = players.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.text)
        }
    }
}
