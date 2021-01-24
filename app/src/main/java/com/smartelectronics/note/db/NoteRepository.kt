package com.smartelectronics.note.db

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData

class NoteRepository(application: Application) {

    private var noteDao: NoteDao
    private var allNote: LiveData<List<Note>>

    init {

        val noteDataBase: NoteDataBase = NoteDataBase.getInstance(application)
        noteDao = noteDataBase.getNoteDao()
        allNote = noteDao.getAllNote()
    }

    fun insert(note: Note){
        InsertNoteAsyncTask(noteDao).execute(note)
    }

    fun update(note: Note){
        UpdateNoteAsyncTask(noteDao).execute(note)
    }

    fun delete(note: Note){
        DeleteNoteAsyncTask(noteDao).execute(note)
    }

    fun deleteAllNote(){
        DeleteAllNoteAsyncTask(noteDao).execute()
    }

    fun getAllNotes(): LiveData<List<Note>>{
        return allNote
    }


    /// AsyncTask operations
    companion object{

        private class InsertNoteAsyncTask(noteDao: NoteDao): AsyncTask<Note, Void, Void>() {

            private var noteDao: NoteDao = noteDao


            override fun doInBackground(vararg notes: Note?): Void? {

                this.noteDao.insert(notes[0]!!)
                return null
            }
        }
        private class UpdateNoteAsyncTask(noteDao: NoteDao): AsyncTask<Note, Void, Void>() {

            private var noteDao: NoteDao = noteDao

            override fun doInBackground(vararg notes: Note?): Void? {

                this.noteDao.update(notes[0]!!)
                return null
            }
        }
        private class DeleteNoteAsyncTask(noteDao: NoteDao): AsyncTask<Note, Void, Void>() {

            private var noteDao: NoteDao = noteDao

            override fun doInBackground(vararg notes: Note?): Void? {

                this.noteDao.delete(notes[0]!!)
                return null
            }
        }
        private class DeleteAllNoteAsyncTask(noteDao: NoteDao): AsyncTask<Void, Void, Void>() {

            private var noteDao: NoteDao = noteDao

            override fun doInBackground(vararg p0: Void?): Void? {

                noteDao.deleteAllNote()
                return null
            }

        }
    }
}