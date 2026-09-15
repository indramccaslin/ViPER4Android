package com.llsl.viper4android.ui.screens.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.llsl.viper4android.R
import com.llsl.viper4android.ui.components.UiDimens
import com.llsl.viper4android.ui.theme.log_level_error
import com.llsl.viper4android.ui.theme.viperBadgeArch
import com.llsl.viper4android.ui.theme.viperBadgeDriver
import com.llsl.viper4android.ui.theme.viperBadgeRate
import com.llsl.viper4android.ui.theme.viperGradientEnd
import com.llsl.viper4android.ui.theme.viperGradientOn
import com.llsl.viper4android.ui.theme.viperGradientStart
import com.llsl.viper4android.ui.theme.viperNeon

private const val BarCount = 21
private val HeroShape = RoundedCornerShape(UiDimens.Large + UiDimens.Small)
private val HeroVisualizerSize = androidx.compose.ui.unit.DpSize(UiDimens.Large * 10, UiDimens.Large * 6)

@Composable
fun HeroCard(
    deviceName: String,
    masterOn: Boolean,
    driverStatus: DriverStatus,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val hubColor = if (masterOn) viperGradientOn else scheme.primary
    val titleColor = if (masterOn) viperGradientOn else scheme.onSurface
    val vizColor = if (masterOn) viperNeon else scheme.primary

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (masterOn) {
                        Modifier.shadow(
                            elevation = UiDimens.Medium + UiDimens.Compact,
                            shape = HeroShape,
                            ambientColor = viperNeon.copy(alpha = 0.35f),
                            spotColor = viperNeon.copy(alpha = 0.50f),
                            clip = false,
                        )
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (masterOn) {
                        Modifier.background(
                            brush =
                                Brush.linearGradient(
                                    colors = listOf(viperGradientStart, viperGradientEnd),
                                ),
                            shape = HeroShape,
                        )
                    } else {
                        Modifier
                    },
                ),
        shape = HeroShape,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (masterOn) {
                        Color.Transparent
                    } else {
                        scheme.surfaceContainerLow.copy(alpha = 0.55f)
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = UiDimens.Standard,
                    vertical = UiDimens.Large,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.hero_master_title).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = hubColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(UiDimens.XSmall + UiDimens.Hairline))
                Text(
                    text = deviceName.ifEmpty { stringResource(R.string.app_name) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
Spacer(modifier = Modifier.height(UiDimens.Small))
                StatusPill(masterOn = masterOn, accent = hubColor)
                Spacer(modifier = Modifier.height(UiDimens.Tiny))
                DriverInfoRow(driverStatus = driverStatus)
                    }
            Spacer(modifier = Modifier.width(UiDimens.Standard))
            VisualizerBars(
                masterOn = masterOn,
                color = vizColor,
                modifier = Modifier.size(HeroVisualizerSize),
            )
        }
    }
}

@Composable
private fun DriverInfoRow(driverStatus: DriverStatus) {
    if (!driverStatus.installed) {
        InfoBadge(
            text = stringResource(R.string.driver_not_found),
            accent = log_level_error,
        )
        return
    }
    val version = "Driver v" + driverStatus.versionName.removePrefix("v")
    val arch =
        when {
            driverStatus.architecture.contains("arm64", ignoreCase = true) ||
                driverStatus.architecture.contains("aarch64", ignoreCase = true) -> "ARM"
            driverStatus.architecture.isNotEmpty() -> driverStatus.architecture.uppercase()
            else -> ""
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiDimens.Compact),
    ) {
        InfoBadge(text = version, accent = viperBadgeDriver)
        if (arch.isNotEmpty()) {
            InfoBadge(text = arch, accent = viperBadgeArch)
        }
        if (driverStatus.samplingRate > 0) {
            InfoBadge(text = "${driverStatus.samplingRate} kHz", accent = viperBadgeRate)
        }
    }
}

@Composable
private fun InfoBadge(
    text: String,
    accent: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clip(RoundedCornerShape(UiDimens.IconLarge))
                .background(accent.copy(alpha = 0.16f))
                .border(UiDimens.Hairline, accent.copy(alpha = 0.45f), RoundedCornerShape(UiDimens.IconLarge))
                .padding(horizontal = UiDimens.Compact, vertical = UiDimens.Tiny),
    ) {
        Canvas(modifier = Modifier.size(UiDimens.Mini)) {
            drawCircle(accent)
        }
        Spacer(modifier = Modifier.width(UiDimens.Tiny))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(masterOn: Boolean, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .clip(RoundedCornerShape(UiDimens.IconLarge * 2))
                .background(accent.copy(alpha = 0.14f))
                .padding(horizontal = UiDimens.Compact, vertical = UiDimens.Tiny),
    ) {
        Canvas(modifier = Modifier.size(UiDimens.Small)) {
            drawCircle(accent)
        }
        Spacer(modifier = Modifier.width(UiDimens.Small))
        Text(
            text = stringResource(if (masterOn) R.string.hero_status_on else R.string.hero_status_off),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun VisualizerBars(
    masterOn: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "visualizer")
    val scale = if (masterOn) 1f else 0.18f
    val barStates =
        List(BarCount) { i ->
            transition.animateFloat(
                initialValue = (0.15f + (i % 5) * 0.03f) * scale,
                targetValue = (0.45f + ((i * 31) % 46) * 0.009f) * scale,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 820 + (i % 6) * 160, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "bar$i",
            )
        }

    Canvas(modifier) {
        val n = barStates.size
        val gap = UiDimens.Tiny.toPx()
        val barWidth = (size.width - gap * (n - 1)) / n
        val minHeight = UiDimens.Mini.toPx()
        barStates.forEachIndexed { index, _ ->
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = if (masterOn) 0.12f else 0.06f),
                topLeft = Offset(x, size.height - barWidth),
                size = Size(barWidth, barWidth),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
        barStates.forEachIndexed { index, state ->
            val barHeight = (size.height * state.value).coerceIn(minHeight, size.height)
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = if (masterOn) 0.95f else 0.40f),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}