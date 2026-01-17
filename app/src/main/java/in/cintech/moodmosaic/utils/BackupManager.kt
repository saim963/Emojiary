package `in`.cintech.moodmosaic.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import `in`.cintech.moodmosaic.data.local.MoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val BACKUP_FILE_NAME = "moodmosaic_backup"
    private const val BACKUP_EXTENSION = ".db"

    /**
     * Export database to a shareable file
     */
    suspend fun exportDatabase(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)

            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database not found"))
            }

            // Close database connections
            MoodDatabase.getDatabase(context).close()

            // Create backup directory
            val backupDir = File(context.cacheDir, "backups")
            backupDir.mkdirs()

            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "${BACKUP_FILE_NAME}_$timestamp$BACKUP_EXTENSION")

            // Copy database to backup file
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Get URI for sharing
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share the backup file
     */
    fun shareBackup(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "MoodMosaic Backup")
            putExtra(Intent.EXTRA_TEXT, "My MoodMosaic data backup. Import this file in MoodMosaic app on your new device.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Backup"))
    }

    /**
     * Import database from URI
     */
    suspend fun importDatabase(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)

            // Close database connections
            MoodDatabase.getDatabase(context).close()

            // Copy imported file to database location
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get database file size
     */
    fun getDatabaseSize(context: Context): String {
        val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)
        return if (dbFile.exists()) {
            val sizeInKB = dbFile.length() / 1024
            if (sizeInKB > 1024) {
                String.format("%.2f MB", sizeInKB / 1024.0)
            } else {
                "$sizeInKB KB"
            }
        } else {
            "0 KB"
        }
    }
}