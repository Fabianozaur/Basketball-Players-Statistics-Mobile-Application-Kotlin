package com.ubb.ubt.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey @ColumnInfo(name = "_id") val _id: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name="number") var number: Int,
    @ColumnInfo(name="ppg") var ppg: Int,
    @ColumnInfo(name="roman") var roman: Boolean
){
    override fun toString(): String {
        val romin = if (roman) "He's romanian" else "He's not romanian"
        return "$name $number $ppg-PPG $romin"
    }
}