package usr.empty.player.items

import kotlinx.serialization.Serializable
import usr.empty.player.database.AppDatabase
import usr.empty.player.database.Track


@Serializable
data class NotaDescriptor(
    val name: String,
    val artist: String,
    val sourceType: Source,
    val source: String,
) {
    companion object {
        fun fromTrack(track: Track, database: AppDatabase): NotaDescriptor {
            return NotaDescriptor(
                track.title, database.artistDao().getArtistByUUID(track.artistId)?.name ?: "unknown", track.sourceType, track.source
            )
        }
    }

    enum class Source {
        LOCAL
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is NotaDescriptor) return false
        if (this.hashCode() != other.hashCode()) return false
        if (this.sourceType != other.sourceType) return false
        if (this.source != other.source) return false
        if (this.artist != other.artist) return false
        if (this.name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + sourceType.hashCode()
        result = 31 * result + source.hashCode()
        return result
    }
}