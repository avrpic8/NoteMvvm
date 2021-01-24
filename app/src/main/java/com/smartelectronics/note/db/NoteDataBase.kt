package com.smartelectronics.note.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartelectronics.note.util.DataConverter

@Database(entities = [Note::class], version = 1 ,exportSchema = false)
@TypeConverters(DataConverter::class)
abstract class NoteDataBase: RoomDatabase() {

    abstract fun getNoteDao(): NoteDao

    companion object{

        //Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: NoteDataBase? = null

        fun getInstance(context: Context): NoteDataBase{

            if(INSTANCE != null) return INSTANCE as NoteDataBase

            synchronized(this){
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDataBase::class.java,
                    "note_dataBase").build()

                INSTANCE = instance
                return instance
            }
        }
    }
}