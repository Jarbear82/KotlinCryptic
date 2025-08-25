package com.tau.cryptic.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun DirectoryPicker(
    show: Boolean,
    onResult: (String?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            onResult(uri?.toString())
        }
    )

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(null)
        }
    }
}