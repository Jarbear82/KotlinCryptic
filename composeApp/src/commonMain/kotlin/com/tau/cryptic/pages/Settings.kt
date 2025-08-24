package com.tau.cryptic.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tau.cryptic.Config

@Composable
fun Settings() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Config.Padding.medium),
        verticalArrangement = Arrangement.spacedBy(Config.Padding.medium)
    ) {
        Text(
            text = "Application Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = Config.theme == Config.AppTheme.DARK,
                onCheckedChange = { isChecked ->
                    Config.theme = if (isChecked) Config.AppTheme.DARK else Config.AppTheme.LIGHT
                }
            )
        }
    }
}