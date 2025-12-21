package `in`.cintech.moodmosaic.di

import `in`.cintech.moodmosaic.data.local.MoodDao
import `in`.cintech.moodmosaic.data.local.MoodDatabase
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MoodDatabase {
        return Room.databaseBuilder(
            context,
            MoodDatabase::class.java,
            MoodDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideMoodDao(database: MoodDatabase): MoodDao {
        return database.moodDao()
    }
}
