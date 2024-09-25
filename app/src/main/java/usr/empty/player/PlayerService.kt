package usr.empty.player

import android.content.Intent
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture


class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        with(ExoPlayer.Builder(this).build()) {
            player = this
            mediaSession = MediaSession.Builder(this@PlayerService, this).setCallback(MediaSessionCallback()).build()
        }
        Log.d("meow", "memememe")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {

    }

    override fun onDestroy() {
        player?.release()
        mediaSession?.release()
        player = null
        mediaSession = null
        Log.d("meow", "owowowow")
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession, controller: MediaSession.ControllerInfo, mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map {
                it.buildUpon().setUri(it.mediaId).build()
            }.toMutableList()

            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
}