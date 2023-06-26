// Copyright (c) 2023
package net.codebot.shared

import java.time.LocalDateTime
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.ZoneOffset

/*
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    /*
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LocalDateTime) = encoder.encodeLong(value.toEpochSecond(ZoneOffset.UTC))
    override fun deserialize(decoder: Decoder) = LocalDateTime.ofEpochSecond(decoder.decodeLong(), 0, ZoneOffset.UTC)

     */
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.ofEpochSecond(decoder.decodeLong(),0, ZoneOffset.UTC)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.toEpochSecond(ZoneOffset.UTC))
    }
}

 */


//@Serializable
class Note (
    var Name: String,
    var RecentlyDeleted: Boolean,
    // @Serializable(LocalDateTimeSerializer::class)
    val DateCreated: Long, //LocalDateTime,
    // @Serializable(LocalDateTimeSerializer::class)
    var DateModified: Long, //LocalDateTime,
    var NotesData: String,
        ) {

    fun print() {
        print(NotesData)
    }

    fun updateNoteData (data: String) {
        NotesData = data
    }

    fun outputNoteData() : String {
        return NotesData
    }

    fun formatDateModified(): LocalDateTime {
        return LocalDateTime.ofEpochSecond(this.DateModified,0, ZoneOffset.UTC)
    }

    fun formatDateCreated(): LocalDateTime {
        return LocalDateTime.ofEpochSecond(this.DateCreated,0, ZoneOffset.UTC)
    }

    override fun toString(): String{
        return Name
    }
}

@Serializable
data class NoteData (
    var Name: String,
    // @Serializable(LocalDateTimeSerializer::class)
    var RecentlyDeleted: Boolean,
    val DateCreated: Long, //LocalDateTime,
    // @Serializable(LocalDateTimeSerializer::class)
    var DateModified: Long, //LocalDateTime,
    var NotesData: String,
    // private var ParentFolder: NotesManager
) {}
