package com.tau.cryptic.components

import androidx.compose.runtime.Composable

@Composable
expect fun DirectoryPicker(
    show: Boolean,
    onResult: (String?) -> Unit
)