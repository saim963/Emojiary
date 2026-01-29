package `in`.cintech.daymoji.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import `in`.cintech.daymoji.data.local.MoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    private const val BACKUP_FILE_PREFIX = "daymoji_backup_"
    private const val BACKUP_EXTENSION = ".db"

    fun generateBackupFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "$BACKUP_FILE_PREFIX$timestamp$BACKUP_EXTENSION"
    }

    /**
     * ✅ FIX: Robust Checkpoint
     * Forces Room to write all temporary data (-wal file) into the main .db file.
     * We actully read the cursor to ensure the operation completes.
     */
    private fun checkpointDatabase(context: Context) {
        try {
            val db = MoodDatabase.getDatabase(context)
            // Execute the checkpoint and consume the result to ensure it finishes
            val cursor = db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val a = cursor.getInt(0)
                    val b = cursor.getInt(1)
                    val c = cursor.getInt(2)
                    Log.d("BackupManager", "Checkpoint result: $a $b $c")
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e("BackupManager", "Checkpoint failed", e)
        }
    }

    suspend fun createShareableBackup(context: Context): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // 1. Force data merge
            checkpointDatabase(context)

            val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database not found"))

            // 2. Create temp file
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, generateBackupFileName())

            // 3. Copy
            dbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 4. Verify file size (Debug)
            Log.d("BackupManager", "Exported file size: ${backupFile.length()} bytes")

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

    suspend fun saveBackupToUri(context: Context, targetUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            checkpointDatabase(context)

            val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)

            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importDatabase(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Aggressively close the database
            val db = MoodDatabase.getDatabase(context)
            if (db.isOpen) {
                db.close()
            }

            val dbFile = context.getDatabasePath(MoodDatabase.DATABASE_NAME)

            // 2. Delete ALL potential Room files to prevent corruption
            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // 3. Copy new file
            context.contentResolver.openInputStream(uri)?.use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot read backup file"))

            // 4. Validate import
            if (dbFile.length() == 0L) {
                return@withContext Result.failure(Exception("Imported file is empty"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    fun shareBackupIntent(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Daymoji Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Backup"))
    }

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