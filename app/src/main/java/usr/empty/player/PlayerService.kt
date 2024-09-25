package usr.empty.player

import android.content.Intent
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentLinkedDeque


class PlayerService : MediaSessionService() {
    private lateinit var mediaSession: MediaSession
    lateinit var player: ExoPlayer
    private var virtualQueue = ConcurrentLinkedDeque<Nota>()

    override fun onCreate() {
        super.onCreate()
        with(ExoPlayer.Builder(this).build()) {
            player = this
            mediaSession = MediaSession.Builder(this@PlayerService, this).setCallback(MediaSessionCallback()).build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("nya", "start!!!")
        intent?.run {
            if (getStringExtra("check") != "empty") return@run
            handleCommand(getStringExtra("type")!!, this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleCommand(type: String, intent: Intent) {
        when (type) {
            "nota" -> {
                virtualQueue.add(Nota(Json.decodeFromString<NotaDescriptor>(intent.getStringExtra("nota")!!)))
//                virtualQueue.first.prepare(player)
                virtualQueue.last.prepare(player)
                player.prepare()
                player.play()
                Log.d("nya", virtualQueue.last.mediaSource.toString())
            }
            "pause" -> player.pause()
            "play" -> player.play()
        }
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
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