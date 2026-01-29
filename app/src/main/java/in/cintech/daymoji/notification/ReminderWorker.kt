package `in`.cintech.daymoji.notification

import android.content.Context
import androidx.work.*
import `in`.cintech.daymoji.data.local.MoodDatabase
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Check if user already logged mood today
            val database = MoodDatabase.getDatabase(applicationContext)
            val todayMood = database.moodDao().getMoodByDate(LocalDate.now())

            if (todayMood == null) {
                // No mood logged today, show reminder
                NotificationHelper.showMoodReminder(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "mood_reminder_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Schedule for 8 PM daily
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun calculateInitialDelay(): Long {
            val now = java.time.LocalDateTime.now()
            var scheduledTime = now.withHour(20).withMinute(0).withSecond(0) // 8 PM

            if (now.isAfter(scheduledTime)) {
                scheduledTime = scheduledTime.plusDays(1)
            }

            return java.time.Duration.between(now, scheduledTime).toMillis()
        }
    }
}