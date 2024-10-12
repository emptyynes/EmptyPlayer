package usr.empty.player

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import usr.empty.player.items.NotaDescriptor
import usr.empty.player.ui.theme.PlayerTheme
import kotlin.math.max
import kotlin.math.min


inline fun <T> nullifyException(block: () -> T) = try {
    block()
} catch (_: Exception) {
    null
}

@UnstableApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", packageName, null)
            intent.setData(uri)
            startActivity(intent)
        }

        startForegroundService(Intent(this, PlayerService::class.java).apply {
            putExtra("check", "empty")
            putExtra("type", "start")
        })

        enableEdgeToEdge()
        setContent {
            PlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun uriToPath(uri: Uri): String {
    val result = uri.pathSegments!![1].replace(Regex("^primary"), "emulated/0").replace(":", "/")
    return "/storage/" + result.replace("primary:", "/storage/emulated/0/")
}

@UnstableApi
@Composable
fun MainLayout(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val queueId = remember { mutableIntStateOf(-1) }
    val notas = remember { EmptyApplication.appInstance.allTracks.toMutableStateList() }
    val isPlaying =
        remember { mutableStateOf(if (PlayerService.isServiceRunning) PlayerService.serviceInstance.player.isPlaying else false) }
    val currentNota =
        remember { mutableStateOf(if (PlayerService.isServiceRunning) PlayerService.serviceInstance.currentTrack?.descriptor else null) }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { audioUri ->
        audioUri?.run {
            uriToPath(this).let {
                MediaMetadataRetriever().apply {
                    setDataSource(it)
                    val nd = NotaDescriptor(name = nullifyException {
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    } ?: it.split('/').last().split('.').first(), artist = nullifyException {
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    } ?: "Unknown Artist", sourceType = NotaDescriptor.Source.LOCAL, source = it)
                    notas.add(nd)
                    queueId.intValue--
                    EmptyApplication.appInstance.addNewTrack(nd)
                }
            }
        }
    }


    Column(modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(shape = RectangleShape, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.secondary
            ), modifier = Modifier, onClick = {
                pickAudioLauncher.launch("audio/*")
            }) {
                Text("add track")
            }
            Button(shape = RectangleShape, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.secondary
            ), modifier = Modifier, onClick = {
                context.startService(Intent(context, PlayerService::class.java).apply {
                    putExtra("check", "empty")
                    putExtra("type", "pause")
                })
            }) {
                Text("| pause |")
            }
            Button(shape = RectangleShape, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.secondary
            ), modifier = Modifier, onClick = {
                context.startService(Intent(context, PlayerService::class.java).apply {
                    putExtra("check", "empty")
                    putExtra("type", "play")
                })
            }) {
                Text("| play >")
            }
        }
        NotaList(notas, modifier = Modifier.weight(1f), queueId = queueId.intValue, playState = isPlaying, currentNota)
        if (isPlaying.value) Player(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .border(1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(25))
        )
    }
}

@UnstableApi
@Composable
fun NotaList(
    notas: List<NotaDescriptor>,
    modifier: Modifier = Modifier,
    queueId: Int = notas.hashCode(),
    playState: MutableState<Boolean>,
    currentNota: MutableState<NotaDescriptor?>
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(notas) { index, nota ->
            val localModifier =
                if (currentNota.value == nota) Modifier.background(MaterialTheme.colorScheme.secondaryContainer) else Modifier
            Column(modifier = localModifier
                .fillParentMaxWidth()
                .clickable {
                    Log.d("meow", "queueId: $queueId")
                    context.startService(Intent(context, PlayerService::class.java).apply {
                        putExtra("check", "empty")
                        putExtra("type", "queue")
                        putExtra("queueId", queueId)
                        putExtra("queue", Json.encodeToString(notas))
                        putExtra("start", index)
                        playState.value = true
                    })
                }
                .padding(4.dp)
                .padding(start = 16.dp)) {
                Text(
                    nota.name, fontSize = 16.sp
                )
                Text(
                    nota.artist, fontSize = 12.sp
                )
            }
        }
    }
}

fun minMax(vMin: Float, x: Float, vMax: Float) = max(min(vMax, x), vMin)

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Player(modifier: Modifier = Modifier) {
    val sliderPosition = remember { mutableFloatStateOf(0.0f) }
    val sliderMovingAllow = remember { mutableStateOf(true) }
    val service = PlayerService.serviceInstance
    LaunchedEffect(Unit) {
        while (true) {
            if (service.player.isPlaying and sliderMovingAllow.value) {
                sliderPosition.floatValue = minMax(0.0f, service.player.currentPosition.toFloat() / service.player.duration.toFloat(), 1.0f)
                delay(service.player.duration / 2000)
                Log.d("meow", "${service.player.duration / 2000}")
            } else delay(10)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .then(modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) { //        Image(
        //            imageVector = Icons.Filled.PlayArrow,
        //            contentDescription = "play",
        //            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        //            modifier = Modifier
        //                .size(50.dp)
        //                .align(Alignment.CenterVertically)
        //        )
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            val nota = service.currentTrack?.descriptor ?: NotaDescriptor(
                "unknown track", "unknown artist", NotaDescriptor.Source.LOCAL, ""
            )
            Text(nota.name, style = MaterialTheme.typography.titleLarge)
            Text(nota.artist, style = MaterialTheme.typography.titleSmall)
            Slider(value = sliderPosition.floatValue, thumb = {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
            }, track = {
                Row(Modifier.clip(RoundedCornerShape(1.dp))) {
                    if (sliderPosition.floatValue != 0.0f) Box(
                        Modifier
                            .weight(sliderPosition.floatValue)
                            .height(2.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                    if (sliderPosition.floatValue != 1.0f) Box(
                        Modifier
                            .weight(1 - sliderPosition.floatValue)
                            .height(2.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            )
                    )
                }
            }, onValueChange = {
                sliderPosition.floatValue = it
                sliderMovingAllow.value = false
            }, onValueChangeFinished = {
                service.player.seekTo((service.player.duration.toFloat() * sliderPosition.floatValue).toLong())
                sliderMovingAllow.value = true
            })
        }
    }
}