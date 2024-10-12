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
data class Album(
    @ColumnInfo val name: String, @ColumnInfo val artistId: UUID
) {
    @PrimaryKey
    var uuid: UUID = UUID.nameUUIDFromBytes("$name$artistId".toByteArray())
}

@Dao
interface AlbumDao {
    @Insert(onConflict = IGNORE)
    fun insertAlbum(album: Album)

    @Query("SELECT * FROM album")
    fun getAllAlbums(): List<Album>

    @Query("SELECT * FROM album WHERE :uuid = uuid")
    fun getAlbumByUUID(uuid: UUID): Album?
}