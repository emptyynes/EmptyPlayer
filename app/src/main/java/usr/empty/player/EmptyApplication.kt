package usr.empty.player

import android.app.Application
import androidx.room.Room
import usr.empty.player.database.AppDatabase
import usr.empty.player.items.AlbumDescriptor
import usr.empty.player.items.ArtistDescriptor
import usr.empty.player.items.NotaDescriptor


class EmptyApplication : Application() {
    companion object {
        private var _appInstance: EmptyApplication? = null

        val appInstance: EmptyApplication
            get() = _appInstance!!
    }

    lateinit var database: AppDatabase

    val allTracks = ArrayList<NotaDescriptor>()

    val allAlbums = ArrayList<AlbumDescriptor>()

    val allArtists = HashMap<String, ArtistDescriptor>()

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "local"
        ).allowMainThreadQueries().build()
        database.artistDao().getAllArtists().forEach { allArtists[it.name ?: "unknown"] = ArtistDescriptor.fromArtist(it) }
        database.albumDao().getAllAlbums().forEach { album ->
            AlbumDescriptor.fromAlbum(album, database).let {
                allAlbums.add(it)
                allArtists[it.artistName]?.albumList?.add(it)
            }
        }
        database.trackDao().getAll().forEach { track ->
            NotaDescriptor.fromTrack(track, database).let {
                allTracks.add(it)
                allArtists[it.artist]?.trackList?.add(it)
            }
        }
        _appInstance = this
    }

    fun addNewTrack(nota: NotaDescriptor) {
        database.addNewTrack(nota)
        allArtists[nota.artist]?.trackList?.add(nota)
        allTracks.add(nota)
    }
}