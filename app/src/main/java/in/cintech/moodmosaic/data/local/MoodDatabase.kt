package `in`.cintech.moodmosaic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MoodEntity::class],
    version = 1,
//    exportSchema = true
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoodDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao

    companion object {
        const val DATABASE_NAME = "mood_mosaic_db"
    }
}