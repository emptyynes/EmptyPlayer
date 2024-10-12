package usr.empty.player.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.PrimaryKey
import androidx.room.Query
import java.util.UUID


@Entity
data class Artist(
    @ColumnInfo val type: ArtistType, @ColumnInfo val name: String?
) {
    enum class ArtistType {
        UNKNOWN, SINGLE, DUET, FEAT, BAND, CIRCLE
    }

    @PrimaryKey
    var uuid: UUID = if (name == null) UUID.randomUUID() else UUID.nameUUIDFromBytes(name.toByteArray())
}

@Dao
interface ArtistDao {
    @Insert(onConflict = IGNORE)
    fun insertArtist(artist: Artist)

    @Query("SELECT * FROM artist")
    fun getAllArtists(): List<Artist>

    @Query("SELECT * FROM artist WHERE :uuid = uuid")
    fun getArtistByUUID(uuid: UUID): Artist?
}