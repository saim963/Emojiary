package `in`.cintech.daymoji.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// ✅ Public (no 'private') so both App and Widget use this exact same instance
val Context.dataStore by preferencesDataStore(name = "settings")