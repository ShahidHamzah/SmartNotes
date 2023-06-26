package net.codebot.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModelTest {

    @Test
    fun check() {
        assertEquals(1,1)
    }

    @Test
    fun clearMemento() {
        var model = Model()
        model.init()
        assertEquals(model.actualFolderList.size, 2)
    }
}