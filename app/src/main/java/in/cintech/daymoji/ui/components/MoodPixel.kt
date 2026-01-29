package `in`.cintech.daymoji.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.cintech.daymoji.domain.model.MoodEntry
import java.time.LocalDate

@Composable
fun MoodPixel(
    mood: MoodEntry?,
    date: LocalDate,
    size: Dp,
    isToday: Boolean,
    isEditable: Boolean = true,  // ✅ NEW parameter
    animationDelay: Int = 0,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val backgroundColor = when {
        mood != null -> mood.color
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    // ✅ Reduce alpha for non-editable past days (without mood)
    val alpha = when {
        mood != null -> 1f  // Always show moods clearly
        isEditable -> 1f    // Editable days are fully visible
        else -> 0.4f        // Non-editable empty days are dimmed
    }

    Box(
        modifier = Modifier
            .size(size)
            .alpha(alpha)  // ✅ Apply alpha
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .clickable(enabled = isEditable) { onClick() },  // ✅ Only clickable if editable
        contentAlignment = Alignment.Center
    ) {
        if (mood != null) {
            Text(
                text = mood.emoji,
                fontSize = (size.value * 0.45f).sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isEditable) 0.7f else 0.4f  // ✅ Dimmer text for non-editable
                )
            )
        }
    }
}

@Composable
fun EmptyPixel(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
    )
}