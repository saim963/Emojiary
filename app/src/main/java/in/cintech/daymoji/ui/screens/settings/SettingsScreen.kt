package `in`.cintech.daymoji.ui.screens.settings

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.cintech.daymoji.data.local.ThemeMode
import `in`.cintech.daymoji.utils.BackupManager
import `in`.cintech.daymoji.widget.MoodWidgetReceiver
import `in`.cintech.daymoji.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeMode: ThemeMode,
    onCycleTheme: () -> Unit,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    val databaseSize = remember { BackupManager.getDatabaseSize(context) }

    // 1. IMPORT Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isImporting = true
                val result = BackupManager.importDatabase(context, it)
                isImporting = false

                result.onSuccess {
                    Toast.makeText(context, "Backup restored! Restarting app...", Toast.LENGTH_LONG).show()
                    // Restart app to reload DB connection
                    // Using basic restart technique:
                    val packageManager = context.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                    val componentName = intent!!.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    context.startActivity(mainIntent)
                    Runtime.getRuntime().exit(0)
                }.onFailure { error ->
                    Toast.makeText(context, "Import failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 2. SAVE TO DEVICE (Download) Launcher
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isExporting = true
                val result = BackupManager.saveBackupToUri(context, it)
                isExporting = false

                result.onSuccess {
                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(context, "Save failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Collect products
    val donationProducts by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = when (themeMode) {
                        ThemeMode.SYSTEM -> Icons.Default.Brightness6
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                    },
                    title = "Theme",
                    subtitle = when (themeMode) {
                        ThemeMode.SYSTEM -> "System default"
                        ThemeMode.LIGHT -> "Light mode"
                        ThemeMode.DARK -> "Dark mode"
                    },
                    onClick = onCycleTheme
                )
            }

            // Widget Section
            SettingsSection(title = "Widget") {
                val context = LocalContext.current
                val appWidgetManager = remember { AppWidgetManager.getInstance(context) }

                SettingsButton(
                    icon = Icons.Default.Widgets,
                    title = "Add to Home Screen",
                    subtitle = "Pin the Mood Widget to your home screen",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                val myProvider = ComponentName(context, MoodWidgetReceiver::class.java)

                                // ✅ Create the callback Intent
                                val successIntent = Intent(context, MoodWidgetReceiver::class.java).apply {
                                    action = "ACTION_WIDGET_PINNED"
                                }

                                val successCallback = PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    successIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                // Request Pin
                                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)

                            } else {
                                Toast.makeText(context, "Pinning not supported by your launcher", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Requires Android 8.0+", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = "Get reminded to log your mood",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }

            // Backup Section
            SettingsSection(title = "Backup & Restore") {
                SettingsInfoItem(
                    icon = Icons.Default.Storage,
                    title = "Data Size",
                    value = databaseSize
                )

                Spacer(modifier = Modifier.height(8.dp))

                // OPTION 1: Share (Existing)
                SettingsButton(
                    icon = Icons.Default.Share,
                    title = "Share Backup",
                    subtitle = "Send via Email, WhatsApp, etc.",
                    isLoading = isExporting,
                    onClick = {
                        scope.launch {
                            isExporting = true
                            val result = BackupManager.createShareableBackup(context)
                            isExporting = false

                            result.onSuccess { uri ->
                                BackupManager.shareBackupIntent(context, uri)
                            }.onFailure { error ->
                                Toast.makeText(context, "Export failed: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // OPTION 2: Save to Device (NEW)
                SettingsButton(
                    icon = Icons.Default.Save,
                    title = "Save to Device",
                    subtitle = "Save to Downloads folder",
                    isLoading = isExporting, // reusing loading state
                    onClick = {
                        // This opens the system file picker to choose WHERE to save
                        saveLauncher.launch(BackupManager.generateBackupFileName())
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Import button
                SettingsButton(
                    icon = Icons.Default.Restore,
                    title = "Import Backup",
                    subtitle = "Restore data from file",
                    isLoading = isImporting,
                    onClick = { showImportDialog = true }
                )
            }
            // Backup Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to Transfer Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = """
                            📤 To backup:
                            1. Tap "Export Backup"
                            2. Share/Save the file to Google Drive, Email, etc.
                            
                            📥 To restore on new phone:
                            1. Install Daymoji
                            2. Go to Settings → Import Backup
                            3. Select your backup file
                            
                            ⚠️ Importing will replace all current data!
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // ✅ NEW: Support Development Section
            SettingsSection(title = "Support") {
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = "Buy me a coffee ☕",
                    subtitle = "Support app development",
                    onClick = {
                        val product = donationProducts.find { it.productId == "donation_coffee" }
                        if (product != null) {
                            viewModel.purchaseDonation(context as Activity, product)
                        } else {
                            Toast.makeText(context, "Store not ready yet", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = BuildConfig.VERSION_NAME
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Import confirmation dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Restore Backup?") },
            text = {
                Text("This will replace ALL your current data. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        importLauncher.launch("*/*")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingsButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}