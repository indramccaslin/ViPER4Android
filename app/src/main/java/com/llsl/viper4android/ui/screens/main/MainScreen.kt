package com.llsl.viper4android.ui.screens.main

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.llsl.viper4android.R
import com.llsl.viper4android.effect.EffectState
import com.llsl.viper4android.ui.components.UiDimens
import com.llsl.viper4android.ui.screens.debug.DebugLogDialog
import com.llsl.viper4android.ui.screens.device.DeviceDialog
import com.llsl.viper4android.ui.screens.preset.PresetDialog
import com.llsl.viper4android.ui.screens.settings.ExcludedAppsDialog
import com.llsl.viper4android.ui.screens.settings.SettingsDialog
import com.llsl.viper4android.ui.screens.settings.UpdateDialog
import com.llsl.viper4android.ui.screens.status.DriverStatusDialog
import com.llsl.viper4android.ui.theme.log_level_error
import com.llsl.viper4android.ui.theme.log_level_info
import com.llsl.viper4android.ui.theme.master_on_container_dark
import com.llsl.viper4android.ui.theme.master_on_container_light
import com.llsl.viper4android.ui.theme.master_on_onContainer_dark
import com.llsl.viper4android.ui.theme.master_on_onContainer_light
import com.llsl.viper4android.ui.theme.status_active_green
import com.llsl.viper4android.ui.theme.viperBgBottom
import com.llsl.viper4android.ui.theme.viperBgMid
import com.llsl.viper4android.ui.theme.viperBgTop
import com.llsl.viper4android.ui.theme.viperGradientEnd
import com.llsl.viper4android.ui.theme.viperGradientOn
import com.llsl.viper4android.ui.theme.viperGradientStart
import com.llsl.viper4android.ui.theme.viperNeon
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.saveSettingsOnBackground()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presets by viewModel.presetList.collectAsStateWithLifecycle()
    val deviceSettings by viewModel.deviceSettingsList.collectAsStateWithLifecycle()
    val driverStatus by viewModel.driverStatus.collectAsStateWithLifecycle()
    val autoStart by viewModel.autoStartEnabled.collectAsStateWithLifecycle()
    val globalMode by viewModel.globalModeEnabled.collectAsStateWithLifecycle()
    val aidlMode by viewModel.aidlModeEnabled.collectAsStateWithLifecycle()
    val debugMode by viewModel.debugModeEnabled.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val excludedApps by viewModel.excludedApps.collectAsStateWithLifecycle()
    val activePresetName by viewModel.activePresetName.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.queryDriverStatus()
            delay(500.milliseconds)
        }
    }

    var showPresetDialog by remember { mutableStateOf(false) }
    var showDriverStatusDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDebugLog by remember { mutableStateOf(false) }
    var showDeviceDialog by remember { mutableStateOf(false) }
    var showExcludedAppsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val appVersionName =
        remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
            } catch (_: Exception) {
                ""
            }
        }

    val clearAllProgressStr = stringResource(R.string.preset_clear_all_progress)
    val clearedStr = stringResource(R.string.preset_cleared)

    if (showExcludedAppsDialog) {
        ExcludedAppsDialog(
            excludedApps = excludedApps,
            onToggle = viewModel::setAppExcluded,
            loadInstalledApps = viewModel::loadInstalledApps,
            onDismiss = { showExcludedAppsDialog = false },
        )
    }

    if (showPresetDialog) {
        PresetDialog(
            presets = presets,
            onSave = viewModel::savePreset,
            onLoad = { id ->
                viewModel.loadPreset(id)
                showPresetDialog = false
            },
            onDelete = viewModel::deletePreset,
            onRename = viewModel::renamePreset,
            onUpdate = viewModel::updatePreset,
            onClearAll = {
                viewModel.clearAllPresets(
                    notificationTitle = clearAllProgressStr,
                    successStr = clearedStr,
                ) { count ->
                    Toast.makeText(context, "$clearedStr: $count", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showPresetDialog = false },
        )
    }

    if (showDriverStatusDialog) {
        LaunchedEffect(Unit) {
            while (true) {
                viewModel.queryDriverStatus()
                delay(500.milliseconds)
            }
        }
        DriverStatusDialog(
            driverStatus = driverStatus,
            onDismiss = { showDriverStatusDialog = false },
        )
    }

    if (showDebugLog) {
        DebugLogDialog(
            onDisableDebug = {
                viewModel.disableDebugMode()
                showDebugLog = false
            },
            onDismiss = { showDebugLog = false },
        )
    }

    if (showDeviceDialog) {
        DeviceDialog(
            devices = deviceSettings,
            activeDeviceId = state.activeDeviceId,
            onRename = viewModel::renameDevice,
            onLoad = viewModel::loadDevicePreset,
            onUpdate = viewModel::saveDevicePreset,
            onDelete = viewModel::deleteDeviceSettings,
            onDismiss = { showDeviceDialog = false },
        )
    }

    val importSuccessStr = stringResource(R.string.import_success)
    val importFailedStr = stringResource(R.string.import_failed)
    val importPresetStr = stringResource(R.string.settings_import_preset)
    val importPresetLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importPresetFiles(uris, notificationTitle = importPresetStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    val importKernelStr = stringResource(R.string.settings_import_kernel)
    val importKernelLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importKernels(uris, notificationTitle = importKernelStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    val importVdcStr = stringResource(R.string.settings_import_vdc)
    val importVdcLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importVdcs(uris, notificationTitle = importVdcStr, successStr = importSuccessStr) { success ->
                    val msg = if (success) importSuccessStr else importFailedStr
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    if (showSettingsDialog) {
        LaunchedEffect(Unit) { viewModel.queryDriverStatus() }
        SettingsDialog(
            autoStartEnabled = autoStart,
            globalModeEnabled = globalMode,
            aidlModeActive = aidlMode,
            debugModeEnabled = debugMode,
            onGlobalModeChanged = viewModel::toggleGlobalMode,
            onOpenExcludedApps = { showExcludedAppsDialog = true },
            driverStatus = driverStatus,
            appVersionName = appVersionName,
            onAutoStartChanged = viewModel::toggleAutoStart,
            onImportPreset = { importPresetLauncher.launch(arrayOf("application/json")) },
            onImportKernel = {
                importKernelLauncher.launch(
                    arrayOf(
                        "audio/*",
                        "application/octet-stream",
                        "*/*",
                    ),
                )
            },
            onDebugUnlocked = viewModel::enableDebugMode,
            onImportVdc = { importVdcLauncher.launch(arrayOf("*/*")) },
            onCheckUpdate = { viewModel.checkForUpdate() },
            onDismiss = { showSettingsDialog = false },
        )
    }

    val updateCheckingStr = stringResource(R.string.update_checking)
    val updateNoApkStr = stringResource(R.string.update_no_apk_asset)
    val updateCheckFailedFmt = stringResource(R.string.update_check_failed)
    LaunchedEffect(updateState.checking) {
        if (updateState.checking) {
            Toast.makeText(context, updateCheckingStr, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(updateState.error) {
        val err = updateState.error
        if (err != null) {
            Toast.makeText(context, updateCheckFailedFmt.format(err), Toast.LENGTH_LONG).show()
            viewModel.dismissUpdate()
        }
    }
    updateState.release?.let { release ->
        UpdateDialog(
            release = release,
            currentVersion = appVersionName,
            upToDate = updateState.upToDate,
            downloading = updateState.downloading,
            downloadProgress = updateState.downloadProgress,
            onDownloadInstall = {
                viewModel.downloadAndInstall(release) {
                    Toast.makeText(context, updateNoApkStr, Toast.LENGTH_SHORT).show()
                }
            },
            onViewOnGithub = {
                context.startActivity(Intent(Intent.ACTION_VIEW, release.htmlUrl.toUri()))
            },
            onDismiss = { viewModel.dismissUpdate() },
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    viperBgTop,
                                    viperBgMid,
                                    viperBgBottom,
                                ),
                        ),
                    )
                    val glowRadius = size.minDimension * 1.4f
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        viperGradientEnd.copy(alpha = 0.30f),
                                        viperGradientStart.copy(alpha = 0.12f),
                                        Color.Transparent,
                                    ),
                                center = Offset(size.width * 0.5f, 0f),
                                radius = glowRadius,
                            ),
                        radius = glowRadius,
                        center = Offset(size.width * 0.5f, -glowRadius * 0.35f),
                    )
                },
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
            Box(
                modifier =
                    Modifier.background(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(viperGradientStart, viperGradientEnd),
                            ),
                    ),
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.app_name))
                            val deviceName = state.activeDeviceName
                            if (deviceName.isNotEmpty()) {
                                val dotColor =
                                    if (state.masterEnable) {
                                        status_active_green
                                    } else {
                                        viperGradientOn
                                    }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(UiDimens.Small)) {
                                        drawCircle(dotColor)
                                    }
                                    Spacer(modifier = Modifier.width(UiDimens.Small))
                                    Text(
                                        text = deviceName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = viperGradientOn,
                                    )
                                }
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = viperGradientOn,
                            actionIconContentColor = viperGradientOn,
                        ),
                    actions = {
                        MasterTopButton(
                            masterOn = state.masterEnable,
                            onToggle = { viewModel.setMasterEnabled(!state.masterEnable) },
                            modifier = Modifier.padding(end = UiDimens.Standard),
                        )
                    },
                )
            }
        },
        bottomBar = {},
    ) { paddingValues ->
        EffectList(
            state = state,
            viewModel = viewModel,
            driverStatus = driverStatus,
            activePreset = activePresetName,
            debugMode = debugMode,
            onOpenDevices = { showDeviceDialog = true },
            onOpenDriverStatus = { showDriverStatusDialog = true },
            onOpenPresets = { showPresetDialog = true },
            onOpenSettings = { showSettingsDialog = true },
            onOpenDebugLog = { showDebugLog = true },
            modifier = Modifier.padding(paddingValues),
        )
        }
    }
}

@Composable
private fun EffectList(
    state: EffectState,
    viewModel: MainViewModel,
    driverStatus: DriverStatus,
    activePreset: String,
    debugMode: Boolean,
    onOpenDevices: () -> Unit,
    onOpenDriverStatus: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDebugLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetAlpha = if (state.masterEnable) 1f else 0.38f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 200),
        label = "effectListAlpha",
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().graphicsLayer { this.alpha = alpha },
        contentPadding = PaddingValues(bottom = UiDimens.Large),
    ) {
        item { Spacer(modifier = Modifier.height(UiDimens.Medium)) }
        item {
            HeroCard(
                deviceName = state.activeDeviceName,
                masterOn = state.masterEnable,
                driverStatus = driverStatus,
                modifier =
                    Modifier.padding(
                        horizontal = UiDimens.Standard,
                        vertical = UiDimens.XSmall,
                    ),
            )
        }
        item {
            QuickActionsRow(
                debugMode = debugMode,
                onOpenDevices = onOpenDevices,
                onOpenDriverStatus = onOpenDriverStatus,
                onOpenPresets = onOpenPresets,
                onOpenSettings = onOpenSettings,
                onOpenDebugLog = onOpenDebugLog,
                modifier =
                    Modifier.padding(
                        horizontal = UiDimens.Standard,
                        vertical = UiDimens.XSmall,
                    ),
            )
        }
        item {
            MainStatusStrip(
                driverStatus = driverStatus,
                activePreset = activePreset,
                onOpenDriverStatus = onOpenDriverStatus,
                onOpenPresets = onOpenPresets,
                modifier =
                    Modifier.padding(
                        horizontal = UiDimens.Standard,
                        vertical = UiDimens.XSmall,
                    ),
            )
        }
        item { MasterLimiterRows(state, viewModel) }

        val effectSections =
            buildList {
                add(sectionEntry("playbackGain", state.playbackGainControl.enable, ::PlaybackGainSection))
                add(sectionEntry("lufs", state.lufs.enable, ::LUFSTargetingSection))
                add(sectionEntry("multiband", state.multibandCompressor.enable, ::MultibandCompressorSection))
                add(sectionEntry("fet", state.fetCompressor.enable, ::FetCompressorSection))
                add(sectionEntry("ddc", state.ddc.enable, ::DdcSection))
                add(sectionEntry("spectrum", state.spectrumExtension.enable, ::SpectrumExtensionSection))
                add(sectionEntry("eq", state.eq.enable, ::EqualizerSection))
                add(sectionEntry("dynEq", state.dynamicEq.enable, ::DynamicEqSection))
                add(sectionEntry("convolver", state.convolver.enable, ::ConvolverSection))
                add(sectionEntry("field", state.fieldSurround.enable, ::FieldSurroundSection))
                add(sectionEntry("diff", state.diffSurround.enable, ::DiffSurroundSection))
                add(sectionEntry("stereo", state.stereoImager.enable, ::StereoImagerSection))
                add(sectionEntry("headphone", state.headphoneSurround.enable, ::HeadphoneSurroundSection))
                add(sectionEntry("reverb", state.reverb.enable, ::ReverberationSection))
                add(sectionEntry("dynSys", state.dynamicSystem.enable, ::DynamicSystemSection))
                add(sectionEntry("tube", state.tubeSimulator.enable, ::TubeSimulatorSection))
                add(sectionEntry("psycho", state.psychoacousticBass.enable, ::PsychoacousticBassSection))
                add(sectionEntry("bass", state.bass.enable, ::ViperBassSection))
                add(sectionEntry("bassMono", state.bassMono.enable, ::ViperBassMonoSection))
                add(sectionEntry("clarity", state.clarity.enable, ::ViperClaritySection))
                add(sectionEntry("cure", state.cure.enable, ::AuditoryProtectionSection))
                add(sectionEntry("analogX", state.analogX.enable, ::AnalogXSection))
                add(sectionEntry("speakerCorrection", state.speakerCorrection.enable, ::SpeakerOptSection))
            }
        effectSections.sortedByDescending { it.enabled }.forEach { entry ->
            item(key = entry.key) { entry.section(state, viewModel) }
        }
    }
}

private data class EffectSectionEntry(
    val key: String,
    val enabled: Boolean,
    val section: @Composable (EffectState, MainViewModel) -> Unit,
)

private fun sectionEntry(
    key: String,
    enabled: Boolean,
    section: @Composable (EffectState, MainViewModel) -> Unit,
) = EffectSectionEntry(key, enabled, section)

@Composable
private fun MasterTopButton(
    masterOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (masterOn) master_on_container_dark else MaterialTheme.colorScheme.surfaceContainerHighest
    val onContainerColor =
        if (masterOn) master_on_onContainer_dark else MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(UiDimens.AppIcon * 2)) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                containerColor.copy(alpha = 0.5f),
                                containerColor.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                        radius = size.minDimension / 2f,
                        center = center,
                    ),
                radius = size.minDimension / 2f,
                center = center,
            )
        }
        Surface(
            shape = CircleShape,
            color = containerColor,
            contentColor = onContainerColor,
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(UiDimens.AppIcon + UiDimens.Compact),
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = stringResource(R.string.master_enable),
                    modifier = Modifier.size(UiDimens.IconSmall),
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    debugMode: Boolean,
    onOpenDevices: () -> Unit,
    onOpenDriverStatus: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDebugLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiDimens.XSmall),
    ) {
        QuickActionButton(
            icon = Icons.Filled.SpeakerGroup,
            label = stringResource(R.string.menu_devices),
            accent = MaterialTheme.colorScheme.tertiary,
            onClick = onOpenDevices,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.Default.Info,
            label = stringResource(R.string.menu_driver_status),
            accent = log_level_info,
            onClick = onOpenDriverStatus,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.Default.LibraryMusic,
            label = stringResource(R.string.menu_presets),
            accent = MaterialTheme.colorScheme.primary,
            onClick = onOpenPresets,
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            icon = Icons.Default.Settings,
            label = stringResource(R.string.menu_settings),
            accent = MaterialTheme.colorScheme.secondary,
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f),
        )
        if (debugMode) {
            QuickActionButton(
                icon = Icons.Default.BugReport,
                label = stringResource(R.string.debug_log_title),
                accent = log_level_error,
                onClick = onOpenDebugLog,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipShape = RoundedCornerShape(UiDimens.Large)
    Column(
        modifier =
            modifier
                .clip(chipShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f))
                .border(UiDimens.Hairline, accent.copy(alpha = 0.35f), chipShape)
                .clickable(onClick = onClick)
                .padding(vertical = UiDimens.Compact),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(UiDimens.IconLarge),
            tint = accent,
        )
        Spacer(modifier = Modifier.height(UiDimens.Compact))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MainStatusStrip(
    driverStatus: DriverStatus,
    activePreset: String,
    onOpenDriverStatus: () -> Unit,
    onOpenPresets: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val driverText =
        when {
            !driverStatus.installed -> stringResource(R.string.driver_not_found)
            driverStatus.streaming -> stringResource(R.string.status_active)
            else -> stringResource(R.string.status_inactive)
        }
    val driverDotColor =
        when {
            !driverStatus.installed -> scheme.error
            driverStatus.streaming -> status_active_green
            else -> scheme.outline
        }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f),
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenDriverStatus)
                        .padding(horizontal = UiDimens.Standard, vertical = UiDimens.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(UiDimens.IconLarge + UiDimens.Compact)
                            .clip(RoundedCornerShape(UiDimens.Small))
                            .background(log_level_info.copy(alpha = 0.13f))
                            .border(UiDimens.Hairline, log_level_info.copy(alpha = 0.32f), RoundedCornerShape(UiDimens.Small)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(UiDimens.IconMedium),
                        tint = log_level_info,
                    )
                }
                Spacer(modifier = Modifier.width(UiDimens.Large))
                Column(verticalArrangement = Arrangement.spacedBy(UiDimens.Tiny)) {
                    Text(
                        text = stringResource(R.string.status_bar_driver),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(UiDimens.Small)) {
                            drawCircle(driverDotColor)
                        }
                        Spacer(modifier = Modifier.width(UiDimens.Small))
                        Text(
                            text = driverText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                        )
                    }
                }
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = UiDimens.Standard)
                        .width(UiDimens.Hairline)
                        .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenPresets)
                        .padding(horizontal = UiDimens.Standard, vertical = UiDimens.Medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(UiDimens.IconLarge + UiDimens.Compact)
                            .clip(RoundedCornerShape(UiDimens.Small))
                            .background(scheme.primary.copy(alpha = 0.13f))
                            .border(UiDimens.Hairline, scheme.primary.copy(alpha = 0.32f), RoundedCornerShape(UiDimens.Small)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        modifier = Modifier.size(UiDimens.IconMedium),
                        tint = if (activePreset.isNotEmpty()) scheme.primary else scheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(UiDimens.Large))
                Column(verticalArrangement = Arrangement.spacedBy(UiDimens.Tiny)) {
                    Text(
                        text = stringResource(R.string.status_bar_preset),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = activePreset.ifEmpty { stringResource(R.string.preset_active_none) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activePreset.isNotEmpty()) scheme.primary else scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
