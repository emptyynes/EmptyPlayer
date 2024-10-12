package usr.empty.player.database

import androidx.room.Database
import androidx.room.RoomDatabase
import usr.empty.player.items.NotaDescriptor

@Database(
    entities = [Track::class, Artist::class, Album::class], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao

    fun addNewTrack(nota: NotaDescriptor) {
        Artist(Artist.ArtistType.UNKNOWN, nota.artist).let { artist ->
            artistDao().insertArtist(artist)
            trackDao().insertTrack(Track(nota.name, artist.uuid, null, nota.sourceType, nota.source))
        }
    }
}