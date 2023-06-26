
package net.codebot.application

class Memento(state: String) {
    private var state = state

    fun returnState(): String {
        return state
    }
}