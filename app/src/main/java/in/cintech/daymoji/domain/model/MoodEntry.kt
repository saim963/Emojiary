package `in`.cintech.daymoji.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class MoodEntry(
    val date: LocalDate,
    val color: Color,
    val emoji: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty(date: LocalDate = LocalDate.now()) = MoodEntry(
            date = date,
            color = Color.Gray,
            emoji = "😊",
            note = ""
        )
    }
}

// Extension functions to convert between Entity and Domain model
fun `in`.cintech.daymoji.data.local.MoodEntity.toDomain() = MoodEntry(
    date = date,
    color = Color(colorHex.toULong()),
    emoji = emoji,
    note = note,
    createdAt = createdAt
)

fun MoodEntry.toEntity() = `in`.cintech.daymoji.data.local.MoodEntity(
    date = date,
    colorHex = color.value.toLong(),
    emoji = emoji,
    note = note,
    createdAt = createdAt
)