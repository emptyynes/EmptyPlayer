package usr.empty.player

import java.util.LinkedList

class NotaQueue : LinkedList<Nota>() {
    var currentId = 0
    val current
        get() = if (currentId < size) this[currentId] else null
}