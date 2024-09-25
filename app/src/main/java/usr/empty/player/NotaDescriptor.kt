package usr.empty.player

import kotlinx.serialization.Serializable


@Serializable
data class NotaDescriptor(
    val name: String,
    val artist: String,
    val sourceType: Source,
    val source: String,
) {
    enum class Source {
        LOCAL
    }
}