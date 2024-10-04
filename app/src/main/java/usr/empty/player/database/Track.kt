package usr.empty.player.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import usr.empty.player.NotaDescriptor
import java.util.UUID

// a.k.a. Nota
@Entity
data class Track(
    @ColumnInfo val title: String,
    @ColumnInfo val artistId: Int,
    @ColumnInfo val albumId: Int?,
    @ColumnInfo val sourceType: NotaDescriptor.Source,
    @ColumnInfo val source: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo
    var uuid: UUID = UUID.nameUUIDFromBytes("${title}${artistId}".toByteArray())
}

@Dao
interface TrackDao {
    @Insert
    fun insertTrack(track: Track)

    @Query("SELECT * FROM track")
    fun getAll(): List<Track>

    @Query("SELECT * FROM track WHERE :artistId = artistId")
    fun getByArtistId(artistId: Int): List<Track>

    @Query("SELECT * FROM track WHERE :albumId = albumId")
    fun getByAlbumId(albumId: Int): List<Track>
}