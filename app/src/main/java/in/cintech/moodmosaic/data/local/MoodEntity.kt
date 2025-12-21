package `in`.cintech.moodmosaic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey
    val date: LocalDate,
    val colorHex: Long,
    val emoji: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)