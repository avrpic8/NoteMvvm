package com.smartelectronics.note.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true) var id:Int,
    val title:String,
    val description:String,
    val backGroundColor:Int,
    val pinedToStatusBar:Int,
    val favorite:Int,
    val attached:Int,
    val fontSize:Int,
    val editedTime:String,
    val imagesPath: ArrayList<String>?,
    val videosPath: ArrayList<String>?): Serializable{

    constructor(
        title: String, description: String, backGroundColor: Int,
        pinedToStatusBar: Int, favorite: Int, attached: Int,
        fontSize: Int, editedTime: String, imagesPath: ArrayList<String>?,
        videosPath: ArrayList<String>?
    )
            :this(0, title, description,
                    backGroundColor, pinedToStatusBar, favorite,
                    attached, fontSize, editedTime,
                    imagesPath, videosPath)
}