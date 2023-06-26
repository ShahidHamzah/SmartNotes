// Copyright (c) 2023
package net.codebot.shared

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

//@Serializable
class NotesManager(
    var Name: String, val notes: MutableList<Note>) {
    fun addNote(note:Note) {
        notes.add(note)
    }

    fun deleteNote(note:Note) {
        notes.remove(note)
    }

    fun deleteAllNotes(){
        notes.clear()
    }


    override fun toString(): String{
        return Name
    }
}

@Serializable
data class NotesManagerData(
    var Name: String,
    var notes: MutableList<NoteData>
)
{}

@Serializable
data class NotesManagerList(
    var notesManagers: MutableList<NotesManagerData> = mutableListOf()
)