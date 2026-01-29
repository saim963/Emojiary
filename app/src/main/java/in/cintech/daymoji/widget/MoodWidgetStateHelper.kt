package `in`.cintech.daymoji.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object MoodWidgetStateHelper {

    fun updateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MoodWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}