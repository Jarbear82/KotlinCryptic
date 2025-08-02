package org.tau.cryptic

import com.kuzudb.Database as KuzuDatabase
import com.kuzudb.Connection as KuzuConnection
import java.nio.file.Files
import java.nio.file.Paths

actual class KuzuDBService {
    private var db: KuzuDatabase? = null
    private var conn: KuzuConnection? = null

    actual fun initialize() {
        try {
            val dbPath = "kuzudb/database" // Changed to a file path
            val dbDir = Paths.get("kuzudb")
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir)
            }
            // Corrected line: Using the full constructor with default values for the other parameters.
            db = KuzuDatabase(dbPath, 1024 * 1024 * 1024L, true, false, 0L, true, -1L)
            conn = KuzuConnection(db)
            println("KuzuDB initialized successfully.")
        } catch (e: Exception) {
            println("Failed to initialize KuzuDB: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun close() {
        try {
            conn?.close()
            db?.close()
            println("KuzuDB connection closed.")
        } catch (e: Exception) {
            println("Failed to close KuzuDB connection: ${e.message}")
        }
    }
}