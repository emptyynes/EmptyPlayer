package usr.empty.player.items

import usr.empty.player.database.Album
import usr.empty.player.database.AppDatabase

class AlbumDescriptor(
    val name: String, val artistName: String?, @Suppress("unused") val trackList: List<NotaDescriptor>
) {
    companion object {
        fun fromAlbum(album: Album, database: AppDatabase): AlbumDescriptor {
            return AlbumDescriptor(
                album.name,
                database.artistDao().getArtistByUUID(album.artistId)?.name,
                database.trackDao().getByAlbumId(album.uuid).map {
                    NotaDescriptor.fromTrack(it, database)
                })
        }
    }
}