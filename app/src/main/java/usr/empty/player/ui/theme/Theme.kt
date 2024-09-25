package usr.empty.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun PlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), //     Dynamic color is available on Android 12+
    dynamicColor: Boolean = true, content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            if (darkTheme) darkColorScheme(
                background = Color.Black
            ) else lightColorScheme(
                background = Color.White
            )
        }

        darkTheme -> darkColorScheme(
            background = Color.Black
        )

        else -> lightColorScheme(
            background = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}