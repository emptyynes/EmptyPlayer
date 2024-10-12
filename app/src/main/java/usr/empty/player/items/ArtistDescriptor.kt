package usr.empty.player.items

import usr.empty.player.database.Artist


class ArtistDescriptor(
    val name: String
) {
    val albumList = ArrayList<AlbumDescriptor>()
    val trackList = ArrayList<NotaDescriptor>()

    companion object {
        fun fromArtist(artist: Artist): ArtistDescriptor {
            return ArtistDescriptor(artist.name ?: "unknown")
        }
    }
}