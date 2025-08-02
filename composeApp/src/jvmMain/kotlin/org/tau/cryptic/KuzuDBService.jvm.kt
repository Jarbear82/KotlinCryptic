package org.tau.cryptic

import com.kuzudb.main.KuzuDatabase
import com.kuzudb.main.KuzuConnection
import java.nio.file.Files
import java.nio.file.Paths

actual class KuzuDBService {
    private var db: KuzuDatabase? = null
    private var conn: KuzuConnection? = null

    actual fun initialize() {
        try {
            val dbPath = "kuzudb"
            val dbDir = Paths.get(dbPath)
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir)
            }
            db = KuzuDatabase(dbPath, 1024 * 1024 * 1024) // 1GB buffer pool size
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