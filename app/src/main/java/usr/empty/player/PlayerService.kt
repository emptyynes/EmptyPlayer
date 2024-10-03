package usr.empty.player

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.serialization.json.Json


@UnstableApi
class PlayerService : MediaSessionService() {
    private lateinit var mediaSession: MediaSession
    lateinit var player: ExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private val notificationId = 1004
    private var virtualQueue = NotaQueue()
    private var queueId = -1

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    if (player.playbackState == Player.STATE_ENDED) next()
                }
                super.onIsPlayingChanged(isPlaying)
            }
        })
        mediaSession = MediaSession.Builder(this, player).setCallback(MediaSessionCallback()).build()
        playerNotificationManager = PlayerNotificationManager.Builder(this, notificationId, "emptyynes")
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player) = "title?"

                override fun createCurrentContentIntent(player: Player) = null

                override fun getCurrentContentText(player: Player) = "context?"

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback) = null
            })
            .build()
            .apply {
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                setPlayer(player)
                setMediaSessionToken(mediaSession.platformToken)
            }

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel("emptyynes", "Channel", NotificationManager.IMPORTANCE_LOW))

        val nBuilder = NotificationCompat.Builder(this, "emptyynes").setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession))
        startForeground(notificationId, nBuilder.build(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("nya", "start!!!")
        intent?.run {
            if (getStringExtra("check") != "empty") return@run
            handleCommand(getStringExtra("type")!!, this)
        }
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun handleCommand(type: String, intent: Intent) {
        when (type) {
            "start" -> {

            }

            "queue" -> {
                Log.d("meow", "queue!")
                intent.getIntExtra("queueId", -1).let { newQueueId ->
                    if (newQueueId == queueId) return@let
                    virtualQueue.clear()
                    Log.d("meow", "queue reset!")
                    virtualQueue.addAll(Json.decodeFromString<List<NotaDescriptor>>(intent.getStringExtra("queue")!!).map { Nota(it) })
                    queueId = newQueueId
                }
                virtualQueue.currentId = intent.getIntExtra("start", -1)
                Log.d("meow", "currentId: ${virtualQueue.currentId}")
                virtualQueue.current?.let {
                    it.prepare(player)
                    player.prepare()
                    player.play()
                }
            }

            "next" -> {

            }

            "nota" -> {
                virtualQueue.clear()
                virtualQueue.add(Nota(Json.decodeFromString<NotaDescriptor>(intent.getStringExtra("nota")!!)))
                virtualQueue.last.prepare(player)
                player.prepare()
                player.play()
                Log.d("nya", virtualQueue.last.mediaSource.toString())
            }

            "pause" -> player.pause()
            "play" -> player.play()
        }
    }

    fun next() {
        virtualQueue.currentId++
        virtualQueue.current?.let {
            it.prepare(player)
            player.prepare()
            player.play()
        }
        Log.d("meow", "next!")
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