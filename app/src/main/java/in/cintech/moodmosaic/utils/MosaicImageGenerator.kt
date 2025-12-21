package `in`.cintech.moodmosaic.utils

import android.content.Context
import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import `in`.cintech.moodmosaic.domain.model.MoodEntry
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

object MosaicImageGenerator {

    private const val IMAGE_SIZE = 1080
    private const val PADDING = 60
    private const val CORNER_RADIUS = 20f

    fun generateMonthMosaic(
        moods: List<MoodEntry>,
        yearMonth: YearMonth,
        context: Context
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_SIZE, IMAGE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.parseColor("#1A1A1A")
        canvas.drawRect(0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat(), paint)

        // Draw decorative gradient border
        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, IMAGE_SIZE.toFloat(), IMAGE_SIZE.toFloat(),
                intArrayOf(
                    Color.parseColor("#6BCB77"),
                    Color.parseColor("#4ECDC4"),
                    Color.parseColor("#E056FD"),
                    Color.parseColor("#FF6B6B")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

        val borderRect = RectF(4f, 4f, IMAGE_SIZE - 4f, IMAGE_SIZE - 4f)
        canvas.drawRoundRect(borderRect, 30f, 30f, gradientPaint)

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val monthName = yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
        canvas.drawText(
            "$monthName ${yearMonth.year}",
            IMAGE_SIZE / 2f,
            PADDING + 40f,
            titlePaint
        )

        // Subtitle
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Mood Mosaic • ${moods.size} days tracked",
            IMAGE_SIZE / 2f,
            PADDING + 75f,
            subtitlePaint
        )

        // Grid setup
        val gridTop = PADDING + 120
        val gridSize = IMAGE_SIZE - (PADDING * 2)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7

        val columns = 7
        val rows = ceil((daysInMonth + firstDayOfWeek) / 7.0).toInt()
        val cellSize = (gridSize / columns).toFloat()
        val cellPadding = 6f

        val moodMap = moods.associateBy { it.date }

        // Day headers
        val dayHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#666666")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }

        val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
        dayLabels.forEachIndexed { index, label ->
            canvas.drawText(
                label,
                PADDING + (index * cellSize) + cellSize / 2,
                gridTop - 10f,
                dayHeaderPaint
            )
        }

        // Draw grid
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

        var dayCounter = 1
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val cellIndex = row * columns + col

                if (cellIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                    val date = yearMonth.atDay(dayCounter)
                    val mood = moodMap[date]

                    val left = PADDING + (col * cellSize) + cellPadding
                    val top = gridTop + (row * cellSize) + cellPadding
                    val right = left + cellSize - (cellPadding * 2)
                    val bottom = top + cellSize - (cellPadding * 2)

                    val rect = RectF(left, top, right, bottom)

                    if (mood != null) {
                        // Fill with mood color
                        cellPaint.color = mood.color.toArgb()
                        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, cellPaint)

                        // Draw emoji
                        textPaint.textSize = cellSize * 0.4f
                        textPaint.color = Color.WHITE
                        canvas.drawText(
                            mood.emoji,
                            left + (cellSize - cellPadding * 2) / 2,
                            top + (cellSize - cellPadding * 2) / 2 + (textPaint.textSize / 3),
                            textPaint
                        )
                    } else {
                        // Empty cell
                        cellPaint.color = Color.parseColor("#2D2D2D")
                        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, cellPaint)

                        // Day number
                        textPaint.textSize = 18f
                        textPaint.color = Color.parseColor("#555555")
                        canvas.drawText(
                            dayCounter.toString(),
                            left + (cellSize - cellPadding * 2) / 2,
                            top + (cellSize - cellPadding * 2) / 2 + 6f,
                            textPaint
                        )
                    }

                    dayCounter++
                }
            }
        }

        // Footer / Branding
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444444")
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Created with Mood Mosaic",
            IMAGE_SIZE / 2f,
            IMAGE_SIZE - PADDING + 20f,
            footerPaint
        )

        return bitmap
    }

    fun generateYearMosaic(
        moods: List<MoodEntry>,
        year: Int,
        context: Context
    ): Bitmap {
        val width = 1080
        val height = 1920

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.parseColor("#0D0D0D")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$year in Pixels", width / 2f, 120f, titlePaint)

        // Subtitle
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${moods.size} days of feelings",
            width / 2f,
            170f,
            subtitlePaint
        )

        // Grid
        val gridTop = 220f
        val gridPadding = 40f
        val gridWidth = width - (gridPadding * 2)

        val startDate = LocalDate.of(year, 1, 1)
        val totalDays = if (LocalDate.of(year, 12, 31).isLeapYear) 366 else 365

        val columns = 20
        val rows = ceil(totalDays / columns.toDouble()).toInt()
        val cellSize = gridWidth / columns
        val cellPadding = 2f

        val moodMap = moods.associateBy { it.date }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (i in 0 until totalDays) {
            val date = startDate.plusDays(i.toLong())
            val mood = moodMap[date]

            val row = i / columns
            val col = i % columns

            val left = gridPadding + (col * cellSize) + cellPadding
            val top = gridTop + (row * cellSize) + cellPadding
            val size = cellSize - (cellPadding * 2)

            val rect = RectF(left, top, left + size, top + size)

            cellPaint.color = mood?.color?.toArgb() ?: Color.parseColor("#2D2D2D")
            canvas.drawRoundRect(rect, 4f, 4f, cellPaint)
        }

        // Legend
        val legendTop = gridTop + (rows * cellSize) + 60
        val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            textSize = 22f
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText("Jan", gridPadding, legendTop, legendPaint)
        canvas.drawText("Dec", width - gridPadding - 40, legendTop, legendPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444444")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Created with Mood Mosaic",
            width / 2f,
            height - 60f,
            footerPaint
        )

        return bitmap
    }
}