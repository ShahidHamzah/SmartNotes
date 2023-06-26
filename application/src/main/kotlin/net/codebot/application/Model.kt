
package net.codebot.application

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.codebot.shared.*
import java.time.LocalDateTime
import java.time.ZoneOffset

class Model {
    var actualFolderList: MutableList<NotesManager> = mutableListOf<NotesManager>()
    var folderList = ListView<NotesManager>()
    var notesList = ListView<Note>()
    var curFolder: NotesManager? = null
    var curNote: Note? = null
    var primaryStage: Stage? = null
    var stateArray: MutableList<Memento> = mutableListOf<Memento>()
    var undoStack: ArrayDeque<Memento> = ArrayDeque()
    var redoStack: ArrayDeque<Memento> = ArrayDeque()
    var numStates = 20


    fun init() {
        createRecentlyDeletedFolder()
        createFolder("Notes")
//        var file = File("file:NoteData.txt")
//        if (file.exists()) {
//            var string = file.readText()
//            var notesData = Json.decodeFromString<List<NotesManagerData>>(string)
//            var savedFolderList = mutableListOf<NotesManager>()
//            for (folder in notesData) {
//                var listofNotes = mutableListOf<Note>()
//                for (note in folder.notes) {
//                    var noteAdder = Note(note.Name, note.DateCreated, note.DateModified, note.NotesData)
//                    listofNotes.add(noteAdder)
//                }
//                var managerAdder = NotesManager(folder.Name, listofNotes)
//                savedFolderList.add(managerAdder)
//            }
//            actualFolderList = savedFolderList
//            folderList.items = rebuildFolderList()
//        }
        curFolder = actualFolderList.first()
    }

    fun initData(data2: String) {
        if(data2 != "{}") {
        var data = Json {ignoreUnknownKeys = true }.decodeFromString<NotesManagerList>(data2)
            var notesData = data.notesManagers
            var savedFolderList = mutableListOf<NotesManager>()

            for (folder in notesData) {
                var listofNotes = mutableListOf<Note>()
                for (note in folder.notes) {
                    var noteAdder =
                        Note(note.Name, note.RecentlyDeleted, note.DateCreated, note.DateModified, note.NotesData)
                    listofNotes.add(noteAdder)
                }
                var managerAdder = NotesManager(folder.Name, listofNotes)
                savedFolderList.add(managerAdder)
            }
            actualFolderList = savedFolderList
            folderList.items = rebuildFolderList()
        } else {
            createFolder("Notes")
            createRecentlyDeletedFolder()
        }

        curFolder = actualFolderList.first()
    }


    fun checkDuplicateFolder(title: String): String {
        if (actualFolderList.any { it.Name == title }) {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Warning"
            alert.headerText = "Folder name already exists"
            alert.contentText = "Please choose a different name"
            alert.showAndWait()
        }
        return title
    }

    fun notesRenameHelper () {
        var clickedOnObject = notesList.selectionModel.selectedItem

        var title: String = ""
        do {
            val dialog = TextInputDialog("")
            dialog.title = "Rename Note"
            dialog.headerText = "Enter New Note Name:"
            val result = dialog.showAndWait()
            if (result.isPresent) {
                title = result.get()
                title = checkDuplicateNotes(title)
                if (title.trim().isEmpty() || title.all { it.isWhitespace() }) {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Error"
                    alert.headerText = "Note title cannot be empty or consist of only spaces"
                    alert.showAndWait()
                }
            } else {
                dialog.close()
            }
        }
        while (curFolder!!.notes.any { it.Name == title } )
        if (title != "") {
            clickedOnObject.Name = title
            clickedOnObject.DateModified = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        }
        notesList.items = rebuildNoteList()
    }


    fun notesMoveFileHelper() {
        var clickedOnObject = notesList.selectionModel.selectedItem
        var currentFolder = curFolder
        val dialog = TextInputDialog("")
        dialog.title = "Moving files"
        dialog.headerText = "Which folder would you like to move your file to?"


        val result = dialog.showAndWait()
        if (!result.isPresent) {
            dialog.close()
        } else {
            val folderDestination = result.get()
            print(folderDestination)

            var folderExists = false
            var newFolder: NotesManager? = null

            for (i in actualFolderList) {
                if (i.Name == folderDestination) {
                    folderExists = true
                    newFolder = i
                }
            }

            if (newFolder != null) {
                if (folderExists && newFolder.Name != "Recently Deleted") {
                    clickedOnObject.RecentlyDeleted = false
                    currentFolder!!.deleteNote(clickedOnObject)
                    newFolder!!.addNote(clickedOnObject)
                    notesList.items = rebuildNoteList()
                } else {
                    val errorAlert = Alert(Alert.AlertType.ERROR)

                    if (newFolder.Name == "Recently Deleted") {
                        //this should just do the same thing as deleting the file...
                        errorAlert.headerText = "Please delete the file instead"
                    } else {
                        errorAlert.headerText = "Folder name provided is invalid. Please try again."
                    }
                    errorAlert.contentText = "ERROR"
                    errorAlert.showAndWait()
                }
            }
            println("after moving the bool value is : " + clickedOnObject.RecentlyDeleted)
        }
    }

    private fun deleteNoteHelper(note: Note) {
        if (note.RecentlyDeleted) {
            curFolder?.deleteNote(note)
            notesList.items = rebuildNoteList()
            clearMemento()
        } else {
            curFolder!!.deleteNote(note)
            var newFolder: NotesManager? = null

            for (i in actualFolderList) {
                if (i.Name == "Recently Deleted") {
                    newFolder = i
                }
            }
            newFolder!!.addNote(note)
            note.RecentlyDeleted = true
            println("after deleting the bool value is : " + note.RecentlyDeleted)

            notesList.items = rebuildNoteList()

        }
    }

    private fun deleteFolderHelper(folder: NotesManager) {
        var RDFolder: NotesManager? = null

        for (i in actualFolderList) {
            if (i.Name == "Recently Deleted") {
                RDFolder = i
            }
        }

        for (note in folder!!.notes) {
            note.RecentlyDeleted = true
            RDFolder!!.addNote(note)
        }
        folder.deleteAllNotes()
        actualFolderList.remove(folder)
        curFolder = null
        folderList.items = rebuildFolderList()
        notesList.items = rebuildNoteList()
    }

    fun emptyFolder() {
        curFolder!!.deleteAllNotes()
        curFolder = null
        notesList.items = rebuildNoteList()
    }

    fun emptyButtonHelper() {
        val confirmation = Alert(Alert.AlertType.CONFIRMATION)
        confirmation.title = "Empty Recently Deleted"
        confirmation.headerText = "Are you sure you want to permanently delete the notes in Recently Deleted?"
        confirmation.contentText = "This cannot be undone."
        val result = confirmation.showAndWait()

        if (result.isPresent) {
            when (result.get()) {
                ButtonType.OK -> emptyFolder()
            }
        }
    }

    fun notesDeleteHelper(){
        var clickedOnObject = notesList.selectionModel.selectedItem


        val confirmation = Alert(Alert.AlertType.CONFIRMATION)
        confirmation.title = "Delete"
        if (clickedOnObject.RecentlyDeleted){
            confirmation.contentText = "This note will be permanently deleted and cannot be undone."
        } else {
            confirmation.contentText = "Do you want to delete this file?"
        }
        val result = confirmation.showAndWait()
        if (result.isPresent) {
            when (result.get()) {
                ButtonType.OK -> deleteNoteHelper(clickedOnObject)
            }
        }
    }

    fun folderDeleteHelper() {
        var clickedOnObject = folderList.selectionModel.selectedItem

        if (clickedOnObject.Name != "Recently Deleted") {
            val confirmation = Alert(Alert.AlertType.CONFIRMATION)
            confirmation.title = "Delete"
            confirmation.contentText = "Do you want to delete this folder?"
            val result = confirmation.showAndWait()
            if (result.isPresent) {
                when (result.get()) {
                    ButtonType.OK -> deleteFolderHelper(clickedOnObject)
                }
            }
        }
        println("do nothing")

    }

    fun folderRenameHelper() {
        var clickedOnObject = folderList.selectionModel.selectedItem

        if (clickedOnObject.Name != "Recently Deleted") {

            var title: String = ""
            do {
                val dialog = TextInputDialog("")
                dialog.title = "Rename Folder"
                dialog.headerText = "Enter New Folder Name:"
                val result = dialog.showAndWait()
                if (result.isPresent) {
                    title = result.get()
                    title = checkDuplicateFolder(title)
                    if (title.trim().isEmpty() || title.all { it.isWhitespace() }) {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Error"
                        alert.headerText = "Note title cannot be empty or consist of only spaces"
                        alert.showAndWait()
                    }
                } else {
                    dialog.close()
                }
            }
            while (actualFolderList.any { it.Name == title })
            if (title != "") {
                clickedOnObject.Name = title
            }
            folderList.items = rebuildFolderList()
        } else {
            println("Do nothing")
        }
    }

    fun createFolderListener(title: String) {
        createFolder(title)
    }

    fun createNoteListener(title: String) {
        createNote(title)
    }

    fun checkDuplicateNotes(title: String): String {
        if (curFolder!!.notes.any { it.Name == title }) {
            val alert = Alert(Alert.AlertType.WARNING)
            alert.title = "Warning"
            alert.headerText = "Note Name Already Exists"
            alert.contentText = "Please Choose A Different Name"
            alert.showAndWait()
        }
        return title
    }

    fun createNote(title: String) {
        //var title = "New Note"
        if (curFolder!!.Name != "Recently Deleted") {
            var recentlyDeleted = false
            val dateCreated = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            var dateModified = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            var noteData = ""
            var newNote = Note(title, recentlyDeleted, dateCreated, dateModified, noteData)
            curFolder!!.addNote(newNote)
            notesList.items = rebuildNoteList()
        } else {
            println("do nothing")
        }

    }

    fun createFolder(title: String) {
        val dateCreated = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        var dateModified = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        var emptyListOfNotes = mutableListOf<Note>()
        var newFolder = NotesManager(title, emptyListOfNotes)
        actualFolderList.add(newFolder)
        folderList.items = rebuildFolderList()
    }

    fun rebuildNoteList(): ObservableList<Note> {
        var returnList: ObservableList<Note> = FXCollections.observableArrayList()
        if (curFolder != null) {
            for (i in curFolder?.notes!!) {
                println(i)
                returnList.add(i)
            }
        }
        return returnList
    }

    fun rebuildFolderList(): ObservableList<NotesManager> {
        var returnList: ObservableList<NotesManager> = FXCollections.observableArrayList()
        for (i in actualFolderList) {
            returnList.add(i)
        }
        return returnList
    }

    fun clearMemento() {
        undoStack.clear()
        redoStack.clear()
    }

    fun createRecentlyDeletedFolder() {
        val dateCreated = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        var dateModified = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        var emptyListOfNotes = mutableListOf<Note>()
        var newFolder = NotesManager("Recently Deleted", emptyListOfNotes)
        actualFolderList.add(newFolder)
        folderList.items = rebuildFolderList()
    }

    fun encodeData(): String {
        var noteData = mutableListOf<NotesManagerData>()


        for (folder in actualFolderList) {
            var ManagerAdder = NotesManagerData(folder.Name, mutableListOf())
            for (note in folder.notes) {
                var noteAdder = NoteData(
                    note.Name,
                    note.RecentlyDeleted,
                    note.DateCreated,
                    note.DateModified,
                    note.outputNoteData()
                )
                ManagerAdder.notes.add(noteAdder)
            }
            noteData.add(ManagerAdder)
        }
        var data = NotesManagerList(noteData)


        return Json.encodeToString(data)
    }


}