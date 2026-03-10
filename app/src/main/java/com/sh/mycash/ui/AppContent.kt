package com.sh.mycash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.sh.mycash.data.preferences.UserPreferences
import com.sh.mycash.ui.navigation.MyCashNavGraph
import com.sh.mycash.ui.theme.MyCashTheme

@Composable
fun AppContent() {
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)

    val darkTheme by userPreferences.darkTheme.collectAsState(initial = null)

    MyCashTheme(darkTheme = darkTheme) {
        MyCashNavGraph()
    }
}
