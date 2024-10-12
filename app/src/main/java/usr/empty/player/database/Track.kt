package usr.empty.player.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import usr.empty.player.items.NotaDescriptor
import java.util.UUID

// a.k.a. Nota
@Entity
data class Track(
    @ColumnInfo val title: String,
    @ColumnInfo val artistId: UUID,
    @ColumnInfo val albumId: UUID?,
    @ColumnInfo val sourceType: NotaDescriptor.Source,
    @ColumnInfo val source: String,
) {
    @PrimaryKey
    var uuid: UUID = UUID.nameUUIDFromBytes("${title}${artistId}".toByteArray())
}

@Dao
interface TrackDao {
    @Insert(onConflict = REPLACE)
    fun insertTrack(track: Track)

    @Query("SELECT * FROM track")
    fun getAll(): List<Track>

    @Query("SELECT * FROM track WHERE :artistId = artistId")
    fun getByArtistId(artistId: UUID): List<Track>

    @Query("SELECT * FROM track WHERE :albumId = albumId")
    fun getByAlbumId(albumId: UUID): List<Track>

    @Query("DELETE FROM track WHERE uuid = :uuid")
    fun deleteByUUID(uuid: UUID)
}