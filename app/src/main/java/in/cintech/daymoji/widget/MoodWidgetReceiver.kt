package `in`.cintech.daymoji.widget

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MoodWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTION_WIDGET_PINNED") {
            Toast.makeText(context, "Widget added successfully! 🎉", Toast.LENGTH_SHORT).show()

            // ✅ FIX: Wait a tiny bit for the widget ID to be registered, then update
            MainScope().launch {
                delay(500) // 500ms delay to ensure widget is attached
                MoodWidgetStateHelper.updateWidget(context)
            }
        }
    }
}