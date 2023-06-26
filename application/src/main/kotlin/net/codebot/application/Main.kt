// Copyright (c) 2023
package net.codebot.application

import javafx.application.Application
import javafx.beans.binding.BooleanBinding
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage
import net.codebot.shared.Note
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.format.DateTimeFormatter
import java.util.prefs.Preferences


class Main : Application() {
    var model: Model = Model()
    val NOTES_ADDRESS = "http://127.0.0.1:8080/notes"
    val USER_ADDRESS = "http://127.0.0.1:8080/user"
    val LOGIN_ADDRESS = "http://127.0.0.1:8080/login"
    val SERVER_ADDRESS = "http://127.0.0.1:8080/notes"
    val client = HttpClient.newBuilder().build()


    override fun start(stage: Stage) {
        var textDisp = HTMLEditor()
        val userPrefs = Preferences.userNodeForPackage(javaClass)
        var userId = userPrefs.get("userId12", "none")
        var loggedIn = false
        var firstInstance = false
        var data = ""
        val Rename = Button("Rename")
        val Delete = Button("Delete")
        val NewFile = Button ("New File")
        val EmptyButton = Button ("Empty")
        val MoveFile = Button("Move File")
        val newFile = MenuItem("New Note")
        val deleteFile = MenuItem("Delete")
        val undoEdit = MenuItem("Undo")
        val redoEdit = MenuItem("Redo")
        val cutEdit = MenuItem("Cut")
        val copyEdit = MenuItem("Copy")
        val pasteEdit = MenuItem("Paste")

        print("Userid is: $userId")
        if (userId == "none") {
            val dialog = Dialog<Boolean>()
            dialog.title = "Create Password"
            dialog.headerText = "Create Password"

            // Set up the login form
            val passwordLabel = Label("Password:")
            val passwordField = PasswordField()
            val loginButton = Button("Save")
            val cancelButton = Button("Cancel")

            // Set up the layout
            val grid = GridPane()
            grid.hgap = 10.0
            grid.vgap = 10.0
            grid.add(passwordLabel, 0, 1)
            grid.add(passwordField, 1, 1)

            val buttons = HBox(10.0)
            buttons.alignment = Pos.CENTER_RIGHT
            buttons.children.addAll(loginButton, cancelButton)

            val vbox = VBox(10.0)
            vbox.children.addAll(grid, buttons)

            dialog.dialogPane.content = vbox

            loginButton.setOnAction {
                val password = passwordField.text
                if (password.isEmpty()) {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Create Password Failed"
                    alert.headerText = "Password cannot be empty"
                    alert.contentText = "Please enter valid password"
                    alert.showAndWait()
                } else {
                    dialog.result = true
                    dialog.close()
                }
            }

            cancelButton.setOnAction {
                dialog.result = false
                dialog.close()
            }

            dialog.showAndWait()
            loggedIn = dialog.result ?: false

            print(passwordField.text)
            try {

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(USER_ADDRESS))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(passwordField.text))
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    userId = response.body()
                    userPrefs.put("userId12", userId)
                } else {
                    val serviceAlert = Alert(Alert.AlertType.WARNING)
                    serviceAlert.title = "Warning"
                    serviceAlert.headerText = "The Service is not working"
                    serviceAlert.contentText = "You will not be able to use or update saved data"
                    serviceAlert.showAndWait()
                }
            } catch(e:java.net.ConnectException) {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Not Connected to Server"
                alert.headerText = "password not saved"
                alert.contentText = "Information will not be saved"
                alert.showAndWait()
            }
        } else {
            val dialog = Dialog<Boolean>()
            dialog.title = "Login"
            dialog.headerText = "Enter Password"

            // Set up the login form
            val passwordLabel = Label("Password:")
            val passwordField = PasswordField()
            val loginButton = Button("Login")
            val cancelButton = Button("Cancel")

            // Set up the layout
            val grid = GridPane()
            grid.hgap = 10.0
            grid.vgap = 10.0
            grid.add(passwordLabel, 0, 1)
            grid.add(passwordField, 1, 1)

            val buttons = HBox(10.0)
            buttons.alignment = Pos.CENTER_RIGHT
            buttons.children.addAll(loginButton, cancelButton)

            val vbox = VBox(10.0)
            vbox.children.addAll(grid, buttons)

            dialog.dialogPane.content = vbox

            loginButton.setOnAction {
                val password = passwordField.text
                // Perform login validation here
                try {
                    val request = HttpRequest.newBuilder()
                        .uri(URI.create("$NOTES_ADDRESS?id=$userId"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(password))
                        .build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() == 200) {
                        data = response.body()
                    }
                    val isValid = data != "incorrect password" //validateLogin(password)
                    if (isValid) {
                        dialog.result = true
                        dialog.close()
                    } else {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.title = "Login Failed"
                        alert.headerText = "Invalid password"
                        alert.contentText = "Please enter valid password"
                        alert.showAndWait()
                    }
                } catch (e: java.net.ConnectException) {
                    dialog.result = true
                    dialog.close()
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Login Failed"
                    alert.headerText = "Not Connected to Server"
                    alert.contentText = "Data will not be saved"
                    alert.showAndWait()
                }
            }
            cancelButton.setOnAction {
                dialog.result = false
                dialog.close()
            }

            dialog.showAndWait()

            loggedIn = dialog.result ?: false
        }

        if (loggedIn) {
            //check if someone is using the account

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$LOGIN_ADDRESS?id=$userId"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userId))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                var tempRes = response.body()
                if (tempRes.equals("false")) { //nobody is logged in to the user on the server, login
                    val request2 = HttpRequest.newBuilder()
                        .uri(URI.create("$LOGIN_ADDRESS?id=$userId"))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build()
                    val response2 = client.send(request2, HttpResponse.BodyHandlers.ofString())
                    if (response2.statusCode() == 200) { //we've logged in on the server end
                        print("logged into server")
                        firstInstance = true
                        if (data == "") {
                            model.init()
                        } else {
                            model.initData(data)
                        }

                        textDisp.disableProperty().bind(object : BooleanBinding() {
                            init {
                                bind(model.folderList.focusedProperty())
                            }

                            override fun computeValue(): Boolean {

                                // you can check if the field is focused
                                // of if it's content is empty etc.
                                return model.folderList.isFocused
                            }
                        })

                    }
                } else {
                    firstInstance = false
                    if (data == "") {
                        model.init()
                    } else {
                        model.initData(data)
                    }
                    textDisp.isDisable = true
                    Delete.isDisable = true
                    newFile.isDisable = true
                    NewFile.isDisable = true
                    deleteFile.isDisable = true
                    Rename.isDisable = true
                    MoveFile.isDisable = true
                    EmptyButton.isDisable = true
                    undoEdit.isDisable = true
                    redoEdit.isDisable = true
                    cutEdit.isDisable = true
                    pasteEdit.isDisable = true
                    copyEdit.isDisable = true
                }
            }



        }




        model.primaryStage = stage
        val border = BorderPane()
        val notesBorder = BorderPane()

        //setup the text diplay listener
        //var textDispListener = HTMLEditorListener(textDisp)

        // SETUP LAYOUT
        //top
        val menuBar = MenuBar()
        val toolbar = ToolBar()
        val vbox = VBox(menuBar, toolbar)

        val os = System.getProperty("os.name").toLowerCase()
        val isMac = os.startsWith("mac")

        val fileMenu = Menu("File")

        var wasLastKeySpace: Boolean = false

        newFile.accelerator = KeyCombination.keyCombination("Meta+N")


        //warn the user if theres no folder and they try to make a note, otherwise theres a problem
        //if model.curFolder is null then dont let the user make a note
        newFile.setOnAction {
            if (model.curFolder != null) {
                var title: String = ""
                do {
                    val dialog = TextInputDialog("")
                    dialog.title = "Create Note"
                    dialog.headerText = "Enter New Note Name:"

                    val result = dialog.showAndWait()
                    if (result.isPresent) {
                        title = result.get()
                        title = model.checkDuplicateNotes(title)
                    } else {
                        dialog.close()
                    }
                }
                while (model.curFolder!!.notes.any { it.Name == title })
                if (title != "") {
                    model.createNoteListener(title)
                }
            }
        }

        val newFolder = MenuItem("New Folder")

        if(!firstInstance) {
            newFolder.isDisable = true
        }
        newFolder.setOnAction {
            var title: String = ""
            do {
                val dialog = TextInputDialog("")
                dialog.title = "Create Folder"
                dialog.headerText = "Enter New Folder Name:"
                val result = dialog.showAndWait()
                if (result.isPresent) {
                    title = result.get()
                    if (model.actualFolderList.any { it.Name == title }) {
                        val alert = Alert(Alert.AlertType.WARNING)
                        alert.title = "Warning"
                        alert.headerText = "Folder name already exists"
                        alert.contentText = "Please choose a different name"
                        alert.showAndWait()
                    }
                    title = model.checkDuplicateFolder(title)
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
            while (model.actualFolderList.any { it.Name == title })
            if (title != "") {
                model.createFolderListener(title)
            }
        }



        deleteFile.accelerator = KeyCodeCombination(KeyCode.BACK_SPACE)


        fileMenu.items.addAll(newFile, newFolder, deleteFile)

        val editMenu = Menu("Edit")


        editMenu.items.addAll(undoEdit, redoEdit, cutEdit, copyEdit, pasteEdit)

        val viewMenu = Menu("View")
        val sortBy = Menu("Sort folder by")
//        viewMenu.items.addAll(sortBy)

        val sortByDateModified = MenuItem("Date modified")
        sortByDateModified.setOnAction {
            model.curFolder?.notes?.sortByDescending { it.DateModified }
            model.notesList.items = FXCollections.observableArrayList(model.curFolder!!.notes)
        }

        val sortByDateCreated = MenuItem("Date created")
        sortByDateCreated.setOnAction {
            model.curFolder?.notes?.sortByDescending { it.DateCreated }
            model.notesList.items = FXCollections.observableArrayList(model.curFolder!!.notes)
        }

        val sortTitle = MenuItem("Title")
        sortTitle.setOnAction {
//            model.curFolder?.notes?.sortBy { it.Name }
            model.curFolder?.notes?.sortWith(Comparator.comparing(Note::Name, String.CASE_INSENSITIVE_ORDER))
            model.notesList.items = FXCollections.observableArrayList(model.curFolder!!.notes)
        }

        sortBy.items.addAll(sortByDateModified, sortByDateCreated, sortTitle)

        menuBar.menus.addAll(fileMenu, editMenu, viewMenu, sortBy)

        // Hotkeys
        if (isMac) {
            newFile.accelerator = KeyCombination.keyCombination("Meta+N")
            newFolder.accelerator = KeyCombination.keyCombination("Shift+Meta+N")
            deleteFile.accelerator = KeyCodeCombination(KeyCode.BACK_SPACE)
            undoEdit.accelerator = KeyCombination.keyCombination("Meta+Z")
            redoEdit.accelerator = KeyCombination.keyCombination("Shift+Meta+Z")
            cutEdit.accelerator = KeyCombination.keyCombination("Meta+X")
            copyEdit.accelerator = KeyCombination.keyCombination("Meta+C")
            pasteEdit.accelerator = KeyCombination.keyCombination("Meta+V")
        }
        else {
            newFile.accelerator =KeyCombination.keyCombination("Ctrl+N")
            newFolder.accelerator = KeyCombination.keyCombination("Shift+Ctrl+N")
            deleteFile.accelerator = KeyCodeCombination(KeyCode.BACK_SPACE)
            undoEdit.accelerator = KeyCombination.keyCombination("Ctrl+Z")
            redoEdit.accelerator = KeyCombination.keyCombination("Shift+Ctrl+Z")
            cutEdit.accelerator = KeyCombination.keyCombination("Ctrl+X")
            copyEdit.accelerator = KeyCombination.keyCombination("Ctrl+C")
            pasteEdit.accelerator = KeyCombination.keyCombination("Ctrl+V")
        }

        //for the left side of the app, need the folder and notes in the folder
        val folderBox = VBox()
        folderBox.prefWidth = 200.0

        folderBox.children.add(model.folderList)
        model.folderList.prefHeightProperty().bind(folderBox.heightProperty())
        val notesBox = VBox()
        notesBox.prefWidth = 200.0

        val searchField = TextField()
        searchField.promptText = "Search"
        searchField.styleClass.add("search-field")

        notesBox.children.addAll(searchField, model.notesList)

        model.notesList.prefHeightProperty().bind(notesBox.heightProperty())
        notesBorder.left = folderBox
        notesBorder.right = notesBox

        model.notesList.setCellFactory {
            object : ListCell<Note>() {
                override fun updateItem(item: Note?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) "" else "${item.Name}\n${item.formatDateModified().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd, hh:mm a"))}"
                }
            }
        }

        // Search
        fun searchNotes(searchTerm: String) {
            if (model.curFolder != null) {
                val filteredNotes = model.curFolder!!.notes.filter { note ->
                    note.Name.contains(searchTerm, ignoreCase = true) ||
                            note.NotesData.contains(searchTerm, ignoreCase = true)
                }
                model.notesList.items.clear()
                model.notesList.items.addAll(filteredNotes)
            }
        }

        searchField.textProperty().addListener { _, _, newValue ->
            searchNotes(newValue)
        }

        // Center
        var imagePane = Pane()
        imagePane.prefWidth = 500.0
        imagePane.prefHeight = 500.0

        // Bottom



        textDisp.addEventFilter(KeyEvent.ANY) { event ->
            if (event.isShortcutDown && event.code == KeyCode.Z && !event.isShiftDown) {
                event.consume()
                //need to revert to the previous state
                if (model.undoStack.size == 1) {
                    var currentState = Memento(model.curNote!!.outputNoteData())
                    var poppedElement = model.undoStack.removeLast()
                    textDisp.htmlText = poppedElement.returnState()
                    if (model.redoStack.size >= model.numStates) {
                        model.redoStack.removeFirst()
                    }
                    model.redoStack.add(currentState)
                    model.curNote?.updateNoteData(textDisp.htmlText)
                    currentState = Memento((model.curNote!!.outputNoteData()))
                    model.undoStack.add(currentState)

                }
                if (model.undoStack.size > 1) {
                    var currentState = Memento(model.curNote!!.outputNoteData())
                    var poppedElement = model.undoStack.removeLast()
                    textDisp.htmlText = poppedElement.returnState()
                    if (model.redoStack.size >= model.numStates) {
                        model.redoStack.removeFirst()
                    }
                    model.redoStack.add(currentState)
                    println("Popped " + model.undoStack.size.toString())
                    model.curNote?.updateNoteData(textDisp.htmlText)

                }

            } else if (event.isShortcutDown && event.code == KeyCode.Z && event.isShiftDown) {
                if (model.redoStack.size >= 1) {
                    var currentState = Memento(model.curNote!!.outputNoteData())
                    var poppedElement = model.redoStack.removeLast()
                    textDisp.htmlText = poppedElement.returnState()
                    if (model.undoStack.size >= model.numStates) {
                        model.undoStack.removeFirst()
                    }
                    model.undoStack.add(currentState)
                    model.curNote?.updateNoteData(textDisp.htmlText)
                }
            }
        }

        textDisp.onKeyReleased = object : EventHandler<KeyEvent> {
            override fun handle(event: KeyEvent) {
                if (isValidEvent(event)) {
                    if (isPasteEvent(event) || event.code == KeyCode.SPACE || event.code == KeyCode.BACK_SPACE) {
                        var curState = Memento(model.curNote!!.outputNoteData())
                        println(curState.returnState())
                        if (model.undoStack.size >= model.numStates) {
                            model.undoStack.removeFirst()
                            model.undoStack.add(curState)
                        }
                        else {
                            model.undoStack.add(curState)
                        }
                        model.redoStack.clear()

                    }
                    model.curNote?.updateNoteData(textDisp.htmlText)
                }
            }

            private fun isValidEvent(event: KeyEvent): Boolean {
                return (!isSelectAllEvent(event)
                        && (isPasteEvent(event) || isUndoEvent(event) || isCharacterKeyReleased(event) ))
            }

            private fun isUndoEvent(event: KeyEvent): Boolean {
                return event.isShortcutDown && event.code == KeyCode.Z
            }

            private fun isSelectAllEvent(event: KeyEvent): Boolean {
                return event.isShortcutDown && event.code === KeyCode.A
            }

            private fun isPasteEvent(event: KeyEvent): Boolean {
                return event.isShortcutDown && event.code === KeyCode.V
            }

            private fun isCharacterKeyReleased(event: KeyEvent): Boolean {

                return true
            }
        }

        textDisp.onMouseClicked = object : EventHandler<MouseEvent> {
            override fun handle(event: MouseEvent?) {
                if (checkEdit(textDisp.htmlText)) {
                    var curState = Memento(model.curNote!!.outputNoteData())
                    model.curNote?.updateNoteData((textDisp.htmlText))
                    println(curState.returnState())
                    if (model.undoStack.size >= model.numStates) {
                        model.undoStack.removeFirst()
                        model.undoStack.add(curState)
                    }
                    else {
                        model.undoStack.add(curState)
                    }
                    model.redoStack.clear()
                }
            }

            private fun checkEdit(html: String): Boolean {
                if (model.curNote == null) {
                    return false
                }
                if (html.length != model.curNote?.outputNoteData()?.length ||
                    html != model.curNote?.outputNoteData()
                ) {
                    return true
                }
                return false
            }
        }

        undoEdit.setOnAction {
            //revert to the previous state
            if (model.undoStack.size == 1) {
                var currentState = Memento(model.curNote!!.outputNoteData())
                var poppedElement = model.undoStack.removeLast()
                textDisp.htmlText = poppedElement.returnState()
                if (model.redoStack.size >= model.numStates) {
                    model.redoStack.removeFirst()
                }
                model.redoStack.add(currentState)
                model.curNote?.updateNoteData(textDisp.htmlText)

                currentState = Memento((model.curNote!!.outputNoteData()))
                model.undoStack.add(currentState)
            }
            if (model.undoStack.size > 1) {
                var currentState = Memento(model.curNote!!.outputNoteData())
                var poppedElement = model.undoStack.removeLast()
                textDisp.htmlText = poppedElement.returnState()
                if (model.redoStack.size >= model.numStates) {
                    model.redoStack.removeFirst()
                }
                model.redoStack.add(currentState)
                println("Popped " + model.undoStack.size.toString())
                model.curNote?.updateNoteData(textDisp.htmlText)

            }
        }

        //when a redo happens, pop the first item on the redo stack
        //and add it to the undo stack
        redoEdit.setOnAction {
            if (model.redoStack.size >= 1) {
                var currentState = Memento(model.curNote!!.outputNoteData())
                var poppedElement = model.redoStack.removeLast()
                textDisp.htmlText = poppedElement.returnState()
                if (model.undoStack.size >= model.numStates) {
                    model.undoStack.removeFirst()
                }
                model.undoStack.add(currentState)
                model.curNote?.updateNoteData(textDisp.htmlText)
            }
        }



        /*
        textDisp.textProperty()
            .addListener(ChangeListener<String>
            {
                    obs, oldVal, newVal -> println(" Text Changed to  $newVal)\n")
                    if (model.curNote != null) {
                        model.curNote?.updateNoteData(newVal)
                    }
            })58-clean-recently-deleted


         */

        var inRD = false
        //need to fix renaming folders when a note is selected...
        model.folderList.onMouseClicked = EventHandler<MouseEvent>() {
            if (firstInstance) {
                inRD = false
                Delete.isDisable = false
                newFile.isDisable = false
                NewFile.isDisable = false
                deleteFile.isDisable = false
                Rename.isDisable = false
                MoveFile.isDisable = false
                EmptyButton.isDisable = true
            } else {
                inRD = false
                Delete.isDisable = true
                newFile.isDisable = true
                NewFile.isDisable = true
                deleteFile.isDisable = true
                Rename.isDisable = true
                MoveFile.isDisable = true
                EmptyButton.isDisable = true
            }

            model.notesList.selectionModel.clearSelection()
            model.curNote = null
            textDisp.htmlText = ""
            // textDisp.clear()
            if (it.button == MouseButton.PRIMARY) {
                if (model.folderList.selectionModel.selectedItem != null) {
                    if (model.folderList.selectionModel.selectedItem!!.Name == "Recently Deleted" && firstInstance){
//                        println("weewooweewoo")
                        Delete.isDisable = true
                        newFile.isDisable = true
                        NewFile.isDisable = true
                        deleteFile.isDisable = true
                        Rename.isDisable = true
                        MoveFile.isDisable = true
                        inRD = true
                        EmptyButton.isDisable = false
                    }
                    model.curFolder = model.folderList.selectionModel.selectedItem
                    model.notesList.items = model.rebuildNoteList()
                    model.clearMemento()
                }
            }
        }



        model.notesList.onMouseClicked = EventHandler<MouseEvent>() {
            if (firstInstance) {
                Delete.isDisable = false
                newFile.isDisable = false
                NewFile.isDisable = false
                deleteFile.isDisable = false
                Rename.isDisable = false
                MoveFile.isDisable = false
                EmptyButton.isDisable = true
            } else {
                Delete.isDisable = true
                newFile.isDisable = true
                NewFile.isDisable = true
                deleteFile.isDisable = true
                Rename.isDisable = true
                MoveFile.isDisable = true
                EmptyButton.isDisable = true
            }
            model.folderList.selectionModel.clearSelection()
            if (it.button == MouseButton.PRIMARY) {
                if (model.notesList.selectionModel.selectedItem != null) {
                    if (inRD){
                        newFile.isDisable = true
                        NewFile.isDisable = true
                        Rename.isDisable = true
                    }
                    //need to display the note that is being selected
                    model.curNote = model.notesList.selectionModel.selectedItem
                    textDisp.htmlText = model.curNote?.outputNoteData()
                    model.clearMemento()
                }
            }
        }

        Rename.setOnAction {
            if (model.notesList.selectionModel.selectedItem != null || model.folderList.selectionModel.selectedItem != null) {
                if (model.folderList.selectionModel.selectedItem == null){
                    // means its model.notesList
                    model.notesRenameHelper()
                } else {
                    model.folderRenameHelper()
                }
            }
        }

        MoveFile.setOnAction{
            //print("move selected ")

            if (model.notesList.selectionModel.selectedItem != null || model.folderList.selectionModel.selectedItem != null) {
                if (model.folderList.selectionModel.selectedItem == null){
                    // means its model.notesList
                    model.notesMoveFileHelper()
                }
            }
        }




        //file menu delete
        deleteFile.setOnAction {
            if (model.notesList.selectionModel.selectedItem != null || model.folderList.selectionModel.selectedItem != null) {
                if (model.folderList.selectionModel.selectedItem == null) {
                    // means its model.notesList
                    model.notesDeleteHelper()
                } else {
                    model.folderDeleteHelper()
                }
            }
        }

        //delete button on toolbar
        Delete.setOnAction {
            if (model.notesList.selectionModel.selectedItem != null || model.folderList.selectionModel.selectedItem != null) {
                if (model.folderList.selectionModel.selectedItem == null) {
                    // means its model.notesList
                    model.notesDeleteHelper()
                } else {
                    model.folderDeleteHelper()
                }
            }
        }

        EmptyButton.setOnAction{
            if (model.notesList.selectionModel.selectedItem != null || model.folderList.selectionModel.selectedItem != null) {
                if (model.notesList.selectionModel.selectedItem == null) {
                    // means its model.notesList
                    model.emptyButtonHelper()
                }
            }

        }

        NewFile.setOnAction {
            if (model.curFolder != null) {
                var title: String = ""
                do {
                    val dialog = TextInputDialog("")
                    dialog.title = "Create Note"
                    dialog.headerText = "Enter New Note Name:"
                    val result = dialog.showAndWait()
                    if (result.isPresent) {
                        title = result.get()
                        title = model.checkDuplicateNotes(title)
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
                while (model.curFolder!!.notes.any { it.Name == title })
                if (title != "") {
                    model.createNoteListener(title)
                }
            }
        }

//        Bold.setOnAction{
//            print(textDisp.htmlText)
//        }

        border.top = vbox
        border.left = notesBorder
        border.center = textDisp

        val scene = Scene(border, 1000.0, 800.0)

        val themeToggle = ToggleButton("Dark Mode")
        themeToggle.setOnAction {
            if (themeToggle.isSelected) {
                scene.getStylesheets().add(javaClass.getResource("/styles/darkMode.css").toExternalForm())
                scene.root.styleClass.add("dark")
                userPrefs.put("theme", "dark")
            } else {
                scene.getStylesheets().remove(javaClass.getResource("/styles/darkMode.css").toExternalForm())
                scene.root.styleClass.remove("dark")
                userPrefs.put("theme", "light")
            }
        }

        val cssPath = javaClass.getResource("/styles/darkMode.css")?.toExternalForm()
        println("Loaded CSS file from: $cssPath")

        toolbar.items.addAll(NewFile, Rename, Delete, MoveFile, EmptyButton, themeToggle)

        stage.minWidth = 550.0
        stage.minHeight = 400.0

        // get window location from user preferences: use x=100, y=100, width=400, height=400 as default
        // get window location from user preferences: use x=100, y=100, width=400, height=400 as default
        val x = userPrefs.getDouble("stage.x", 100.0)
        val y = userPrefs.getDouble("stage.y", 100.0)
        val w = userPrefs.getDouble("stage.width", 1200.0)
        val h = userPrefs.getDouble("stage.height", 400.0)
        val theme = userPrefs.get("theme", "light")
        if (theme == "dark") {
            scene.getStylesheets().add(javaClass.getResource("/styles/darkMode.css").toExternalForm())
            scene.root.styleClass.add("dark")
            themeToggle.isSelected = true
        } else {
            scene.getStylesheets().remove(javaClass.getResource("/styles/darkMode.css").toExternalForm())
            scene.root.styleClass.remove("dark")
            scene.root.styleClass.add("light")
        }

        stage.x = x
        stage.y = y
        stage.width = w
        stage.height = h
        stage.scene = scene

        stage.title = "NotesApp"
        stage.show()
    }

    override fun stop() {
        // super.stop()
        val userPrefs = Preferences.userNodeForPackage(javaClass)
        model.primaryStage?.let { userPrefs.putDouble("stage.x", it.x) }
        model.primaryStage?.let { userPrefs.putDouble("stage.y", it.y) }
        model.primaryStage?.let { userPrefs.putDouble("stage.width", it.width) }
        model.primaryStage?.let { userPrefs.putDouble("stage.height", it.height) }

        println("close")


        var data = model.encodeData()


        var userId = userPrefs.get("userId12", "none")
        try {
            //need to logout, but first check the login status
            var loginStatus = "false"
            val request0 = HttpRequest.newBuilder()
                .uri(URI.create("$LOGIN_ADDRESS?id=$userId"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userId))
                .build()
            val response0 = client.send(request0, HttpResponse.BodyHandlers.ofString())
            if (response0.statusCode() == 200) {
                loginStatus = response0.body()
            }

            //only log out if you're not logged in and you are the first instance (the one that can edit)
            if (loginStatus != "false" ) {
                val request1 = HttpRequest.newBuilder()
                    .uri(URI.create("$LOGIN_ADDRESS?id=$userId"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(userId))
                    .build()
                val response1 = client.send(request1, HttpResponse.BodyHandlers.ofString())
                if (response1.statusCode() == 200) {
                    var tempRes = response1.body()
                    println("after logging out, the user is: " + tempRes)
                }
            }

            val request = HttpRequest.newBuilder()
                .uri(URI.create("$NOTES_ADDRESS?id=$userId"))                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(data))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (e:java.net.ConnectException) {
            val serviceAlert = Alert(Alert.AlertType.WARNING)
            serviceAlert.title = "Warning"
            serviceAlert.headerText = "The Service is not working"
            serviceAlert.contentText = "Data was not updated"
            serviceAlert.showAndWait()
        }





        /*
        val string = Json.encodeToString(actualFolderList)
        println("String: $string")

         */



        /*
        for (folder in actualFolderList) {
            var string = Json.encodeToString(folder)
            println(string)
            for (note in folder.notes) {
                var note = Json.encodeToString(note)
                println(note)
            }
        }

         */
    }


    }
