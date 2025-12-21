package `in`.cintech.moodmosaic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object EmojiCategories {
    val smileys = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
        "🙂", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗",
        "😚", "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗",
        "🤭", "🤫", "🤔", "🤐", "🤨", "😐", "😑", "😶"
    )

    val emotions = listOf(
        "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "😌", "😔",
        "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
        "🥴", "😵", "😵‍💫", "🤯", "🤠", "🥳", "🥸", "😎",
        "🤓", "🧐", "😕", "😟", "🙁", "😮", "😯", "😲"
    )

    val feelings = listOf(
        "😳", "🥺", "😦", "😧", "😨", "😰", "😥", "😢",
        "😭", "😱", "😖", "😣", "😞", "😓", "😩", "😫",
        "🥱", "😤", "😡", "😠", "🤬", "😈", "👿", "💀",
        "☠️", "💩", "🤡", "👹", "👺", "👻", "👽", "🤖"
    )

    val activities = listOf(
        "💪", "🙏", "👍", "👎", "👊", "✊", "🤝", "👏",
        "🎉", "🎊", "🎈", "🎁", "🎀", "🏆", "🥇", "🎯",
        "🧘", "🏃", "🚶", "💃", "🕺", "🎭", "🎨", "🎬",
        "🎤", "🎧", "🎵", "🎶", "📚", "✍️", "💼", "💻"
    )

    val nature = listOf(
        "☀️", "🌙", "⭐", "🌈", "☁️", "⛅", "🌤️", "🌧️",
        "⛈️", "❄️", "🌸", "🌺", "🌻", "🌼", "🌷", "🌱",
        "🌲", "🌳", "🍀", "🍁", "🍂", "🍃", "🌿", "🌾",
        "🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🐨", "🦋"
    )

    val food = listOf(
        "☕", "🍵", "🧃", "🍷", "🍺", "🍕", "🍔", "🍟",
        "🌮", "🍜", "🍣", "🍦", "🎂", "🍰", "🍪", "🍫",
        "🍬", "🍭", "🍿", "🧁", "🥤", "🍩", "🥐", "🥞"
    )

    val allEmojis = smileys + emotions + feelings + activities + nature + food

    val categories = mapOf(
        "Smileys" to smileys,
        "Emotions" to emotions,
        "Feelings" to feelings,
        "Activities" to activities,
        "Nature" to nature,
        "Food" to food
    )
}

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Smileys") }

    Column(modifier = modifier) {
        Text(
            text = "Pick an emoji",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = EmojiCategories.categories.keys.indexOf(selectedCategory),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            edgePadding = 0.dp
        ) {
            EmojiCategories.categories.keys.forEachIndexed { _, category ->  // ✅ Changed index to _
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ FIXED: Use Column + Row instead of LazyVerticalGrid
        val emojis = EmojiCategories.categories[selectedCategory] ?: emptyList()
        val columns = 8
        val rows = (emojis.size + columns - 1) / columns

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)  // Limit max height
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (columnIndex in 0 until columns) {
                        val index = rowIndex * columns + columnIndex
                        if (index < emojis.size) {
                            val emoji = emojis[index]
                            EmojiItem(
                                emoji = emoji,
                                isSelected = emoji == selectedEmoji,
                                onClick = { onEmojiSelected(emoji) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmojiItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "emoji_scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuickEmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickEmojis = listOf(
        "😊", "😢", "😡", "😴", "🥳", "😰", "🥰", "😤",
        "🤔", "😌", "🙏", "💪", "☀️", "🌧️", "❤️", "✨"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        quickEmojis.take(8).forEach { emoji ->
            EmojiItem(
                emoji = emoji,
                isSelected = emoji == selectedEmoji,
                onClick = { onEmojiSelected(emoji) }
            )
        }
    }
}