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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.room.Room
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import usr.empty.player.database.AppDatabase
import usr.empty.player.database.Track
import usr.empty.player.ui.theme.PlayerTheme


inline fun <T> nullifyException(block: () -> T) = try {
    block()
} catch (_: Exception) {
    null
}

@UnstableApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //        Log.d("meow", "${filesDir.toPath().resolve("all_tracks.data")}")
        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "local"
        ).allowMainThreadQueries().build()
        Log.d("meow", "${db.trackDao().getAll()}")

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
    val notas = remember { mutableStateListOf<NotaDescriptor>() }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { audioUri ->
        audioUri?.run {
            uriToPath(this).let {
                MediaMetadataRetriever().apply {
                    setDataSource(it) //                    notas.add(NotaDescriptor(name = nullifyException {
                    //                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    //                    } ?: it.split('/').last().split('.').first(), artist = nullifyException {
                    //                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    //                    } ?: "Unknown Artist", sourceType = NotaDescriptor.Source.LOCAL, source = it))
                    val db = Room.databaseBuilder(
                        context.applicationContext, AppDatabase::class.java, "local"
                    ).allowMainThreadQueries().build()
                    db.trackDao().insertTrack(Track(title = nullifyException {
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    } ?: it.split('/').last().split('.').first(),
                                                    artistId = 0,
                                                    albumId = null,
                                                    sourceType = NotaDescriptor.Source.LOCAL,
                                                    source = it))
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
//            VerticalDivider()
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
        NotaList(notas)
    }
}

@UnstableApi
@Composable
fun NotaList(notas: List<NotaDescriptor>, modifier: Modifier = Modifier, queueId: Int = notas.hashCode()) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier,
    ) {
        itemsIndexed(notas) { index, nota ->
            Column(modifier = Modifier
                .fillParentMaxWidth()
                .clickable {
                    Log.d("meow", "queueId: $queueId")
                    context.startService(Intent(context, PlayerService::class.java).apply {
                        putExtra("check", "empty")
                        putExtra("type", "queue")
                        putExtra("queueId", queueId)
                        putExtra("queue", Json.encodeToString(notas))
                        putExtra("start", index)
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

@UnstableApi
@Preview
@Composable
fun Preview() {
    NotaList(
        listOf(
            NotaDescriptor(name = "hello", artist = "there", sourceType = NotaDescriptor.Source.LOCAL, source = ""),
            NotaDescriptor(name = "preview", artist = "me", sourceType = NotaDescriptor.Source.LOCAL, source = ""),
        )
    )
}