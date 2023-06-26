// Copyright (c) 2023
package com.example.demo

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.io.File
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

//Need to create user as soon as user launches application first time
//with empty data.
object Users : IntIdTable() {
    val data = varchar("data", 1000000000) // Column<String>
    val pswd = varchar("password", 100) // Column<String>
    val loggedIn = varchar("loggedIn", 10) // Column<Boolean>
}


@SpringBootApplication
class Server

fun main(args: Array<String>) {
    runApplication<Server>(*args)
}

//Service Code below
/*
@RestController
@RequestMapping("/notes")
class NotesResource(val service: NoteService) {
    @GetMapping
    fun all() = service.getNotes()
    // fun user(id: Int, password: String): String? = service.getNotes(id, password)
    @PostMapping
    fun post(@RequestBody noteData: MutableList<NotesManagerData>) {
        service.postNotes(noteData)
    }
}

 */

@RestController
@RequestMapping("/notes")
class NotesResource(val service: NotesService) {
    //@GetMapping
    //fun user(id: Int) = service.getNotes(id)
    //fun index(): List<Message> = service.findMessages()

    @PostMapping
    fun getUser(id: Int, @RequestBody password: String): String? {
        return service.getNotes(id, password)
    }

    @PutMapping
    fun put(id: Int, @RequestBody data: String) {
        service.updateNotes(id, data)
    }
}

@RestController
@RequestMapping("/user")
class UserResource(val service: NotesService) {
    @PostMapping
    fun post(@RequestBody password: String): String {
        return service.createUser(password)
    }
}

@RestController
@RequestMapping("/login")
class LoginResource(val service: NotesService) {
    @GetMapping
    fun login(id: Int) {
        service.login(id)
    }

    @PutMapping
    // Logout
    fun logout(id: Int) {
        service.logout(id)
    }

    @PostMapping
    // Checks if logged in
    fun getLogin(id: Int): String? {
        return service.checkLogin(id)
    }
}



//Database/JSON code below
//data class Message(val id: String, val text: String)


@Service
class NotesService {


    init {
        Database.connect("jdbc:h2:notesData.db")
        transaction {
            SchemaUtils.create(Users)
        }
    }



    // Will need:
    //  Create user (POST request with empty data that returns ID)
    //  Get User Data (GET Request that gives user id and gets user data)
    //  Update User Data (POST request that gives user id and data and updates that field)

    fun createUser(password: String): String {
        var id: EntityID<Int>? = null
        transaction {
            addLogger(StdOutSqlLogger)
            id = Users.insertAndGetId {
                it[data] = ""
                it[pswd] = password
                it[loggedIn] = "false"
            }
        }
        return id.toString()
    }

    fun getNotes(id: Int, password: String): String? {
        var data: ResultRow? = null
        transaction {
            addLogger(StdOutSqlLogger)
            data = Users.select(Users.id eq id).single()
        }
        return if (data?.get(Users.pswd) != password) {
            "incorrect password"
        } else {
            data?.get(Users.data)?.toString()
        }
    }

    fun updateNotes(id: Int, data: String) {
        transaction {
            addLogger(StdOutSqlLogger)
            Users.update ({ Users.id eq id}) {
                it[Users.data] = data
            }
        }
    }

    fun login(id: Int) {
        transaction {
            addLogger(StdOutSqlLogger)
            Users.update({ Users.id eq id}) {
                it[Users.loggedIn] = "true"
            }
        }
    }

    fun logout(id: Int) {
        transaction {
            addLogger(StdOutSqlLogger)
            Users.update({ Users.id eq id}) {
                it[Users.loggedIn] = "false"
            }
        }
    }

    fun checkLogin(id: Int): String? {
        var data: ResultRow? = null
        transaction {
            addLogger(StdOutSqlLogger)
            data = Users.select(Users.id eq id).single()
        }
        return data?.get(Users.loggedIn)
    }

}

