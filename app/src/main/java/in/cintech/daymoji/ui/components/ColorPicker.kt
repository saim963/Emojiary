package `in`.cintech.daymoji.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.cintech.daymoji.ui.theme.MoodColors

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "How do you feel?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ FIXED: Use Column + Row instead of LazyVerticalGrid
        val colors = MoodColors.allColors
        val columns = 5
        val rows = (colors.size + columns - 1) / columns

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (columnIndex in 0 until columns) {
                        val index = rowIndex * columns + columnIndex
                        if (index < colors.size) {
                            val color = colors[index]
                            ColorItem(
                                color = color,
                                isSelected = color == selectedColor,
                                colorName = MoodColors.colorNames[color] ?: "",
                                onClick = { onColorSelected(color) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty space for incomplete rows
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorItem(
    color: Color,
    isSelected: Boolean,
    colorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "color_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, Color.White.copy(alpha = 0.7f))
                            ),
                            shape = CircleShape
                        )
                    } else Modifier
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = colorName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (isSelected) 1f else 0.6f
            )
        )
    }
}

@Composable
fun CustomColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0.5f) }
    var lightness by remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(hue, saturation, lightness) {
        onColorSelected(Color.hsl(hue, saturation, lightness))
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Custom Color",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Color Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(selectedColor)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hue Slider
        Text(text = "Hue", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color.hsl(hue, 1f, 0.5f),
                activeTrackColor = Color.hsl(hue, 1f, 0.5f)
            )
        )

        // Saturation Slider
        Text(text = "Saturation", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = saturation,
            onValueChange = { saturation = it },
            valueRange = 0f..1f
        )

        // Lightness Slider
        Text(text = "Lightness", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = lightness,
            onValueChange = { lightness = it },
            valueRange = 0.2f..0.8f
        )
    }
}