package com.example.booktracker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Book (
    @PrimaryKey(autoGenerate = true) val id : Long,
    @ColumnInfo val title : String,
    @ColumnInfo val author : String,
    @ColumnInfo val description : String,
    @ColumnInfo val link : String,
    @ColumnInfo var image : String,
    @ColumnInfo var read : Int
) {

    override fun toString() : String {
        return "$title by $author"
    }

    fun toStringMain() : String {
        return "<h3>$title</h3>\n<p>$author</p>"
    }

}