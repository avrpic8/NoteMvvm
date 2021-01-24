package com.smartelectronics.note.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NoteViewModel(application: Application): AndroidViewModel(application) {

    private val noteRepository: NoteRepository?
    private val allNote: LiveData<List<Note>>

    init {
        noteRepository = NoteRepository(application)
        allNote = noteRepository.getAllNotes()
    }



    fun insert(note: Note){
        noteRepository?.insert(note)
    }
    fun update(note: Note){
        noteRepository?.update(note)
    }
    fun delete(note: Note){
        noteRepository?.delete(note)
    }
    fun deleteAllNotes(){
        noteRepository?.deleteAllNote()
    }
    fun getAllNotes(): LiveData<List<Note>>{
        return allNote
    }
}