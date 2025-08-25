package com.tau.cryptic.components

import androidx.compose.runtime.Composable
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
actual fun DirectoryPicker(
    show: Boolean,
    onResult: (String?) -> Unit
) {
    if (show) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Select a Directory"
        }

        val result = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile: File = fileChooser.selectedFile
            onResult(selectedFile.absolutePath)
        } else {
            onResult(null)
        }
    }
}