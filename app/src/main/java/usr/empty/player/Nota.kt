package usr.empty.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer


class Nota(val descriptor: NotaDescriptor) {
    val mediaSource = MediaItem.fromUri(descriptor.source)

    fun prepare(player: ExoPlayer) {
        player.setMediaItem(mediaSource)
    }
}