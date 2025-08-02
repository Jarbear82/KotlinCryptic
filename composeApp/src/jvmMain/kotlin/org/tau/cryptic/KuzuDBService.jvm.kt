package org.tau.cryptic

import com.kuzudb.Database as KuzuDatabase
import com.kuzudb.Connection as KuzuConnection
import java.nio.file.Files
import java.nio.file.Paths
import org.tau.cryptic.pages.EdgeSchema
import org.tau.cryptic.pages.NodeSchema
import org.tau.cryptic.pages.PropertyType

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

    /**
     * Creates a node table in the database from a NodeSchema.
     */
    fun createNodeSchema(graphName: String, schema: NodeSchema) {
        val tableName = "${graphName}_${schema.typeName}"
        val properties = schema.properties.joinToString(", ") {
            "${it.key.replace(" ", "_")} ${mapPropertyType(it.type)}"
        }
        // The PRIMARY KEY (name) assumes 'name' is a unique identifier.
        val query = "CREATE NODE TABLE $tableName (id STRING, $properties, PRIMARY KEY (id))"
        try {
            println("Executing query: $query")
            conn?.query(query)
            println("Successfully created node table: $tableName")
        } catch (e: Exception) {
            println("Failed to create node table '$tableName': ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Creates a relationship table in the database from an EdgeSchema.
     */
    fun createEdgeSchema(graphName: String, schema: EdgeSchema, fromTable: String, toTable: String) {
        val tableName = "${graphName}_${schema.typeName}"
        val fromTableName = "${graphName}_$fromTable"
        val toTableName = "${graphName}_$toTable"
        val properties = schema.properties.joinToString(", ") {
            "${it.key.replace(" ", "_")} ${mapPropertyType(it.type)}"
        }
        // Note: KuzuDB requires REL tables to be defined with FROM and TO tables.
        val query = "CREATE REL TABLE $tableName (FROM $fromTableName TO $toTableName, $properties)"
        try {
            println("Executing query: $query")
            conn?.query(query)
            println("Successfully created edge table: $tableName")
        } catch (e: Exception) {
            println("Failed to create edge table '$tableName': ${e.message}")
            e.printStackTrace()
        }
    }

    private fun mapPropertyType(type: PropertyType): String {
        return when (type) {
            PropertyType.TEXT, PropertyType.LONG_TEXT, PropertyType.IMAGE -> "STRING"
            PropertyType.NUMBER -> "INT64" // Using INT64 for numbers
            PropertyType.BOOLEAN -> "BOOLEAN"
            PropertyType.DATE -> "DATE"
            PropertyType.TIMESTAMP -> "TIMESTAMP"
            // For complex types, we'll serialize them as STRING for now.
            PropertyType.LIST, PropertyType.MAP, PropertyType.VECTOR, PropertyType.STRUCT -> "STRING"
        }
    }
}