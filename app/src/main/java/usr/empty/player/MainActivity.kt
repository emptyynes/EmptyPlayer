package usr.empty.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import usr.empty.player.ui.theme.PlayerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, PlayerService::class.java))
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

@Composable
fun MainLayout(modifier: Modifier = Modifier) {
    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { audioUri ->
        audioUri?.run {
            path?.let {
                Log.d("meow", it)
            }
        }
    }
    Column (modifier.fillMaxSize()) {
        Button(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = {
            pickAudioLauncher.launch("audio/*")
        }) {
            Text("add track")
        }
    }
}