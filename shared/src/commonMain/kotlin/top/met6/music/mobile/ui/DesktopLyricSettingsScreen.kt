package top.met6.music.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.met6.music.mobile.state.AppState
import top.met6.music.mobile.lyric.checkOverlayPermission
import top.met6.music.mobile.lyric.isAndroid
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopLyricSettingsScreen() {
    // If user returns from settings, we can check overlay permission status.
    // If permission is now granted, we do not auto-enable but user can toggle it.
    LaunchedEffect(Unit) {
        if (isAndroid() && AppState.desktopLyricEnabled.value && !checkOverlayPermission(AppState.context)) {
            // Re-sync state in case permission was revoked externally
            AppState.setDesktopLyricEnabled(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "桌面歌词设置") },
                navigationIcon = {
                    IconButton(onClick = { AppState.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Card 1: Switches
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Switch 1: Enable Desktop Lyric
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "开启桌面歌词",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "开启后在桌面及其他应用上方显示悬浮窗歌词",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = AppState.desktopLyricEnabled.value,
                            onCheckedChange = { AppState.setDesktopLyricEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicPink
                            )
                        )
                    }
                    
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    
                    // Switch 2: Click Play/Pause
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "点击播放暂停",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "开启后点击桌面歌词悬浮窗可控制音乐播放与暂停",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = AppState.desktopLyricClickPlayPause.value,
                            onCheckedChange = { AppState.setDesktopLyricClickPlayPause(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicPink
                            )
                        )
                    }

                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                    // Switch 3: Show when paused
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "暂停时显示歌词/歌名",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "关闭后音乐暂停时将自动隐藏桌面歌词悬浮窗",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = AppState.desktopLyricShowWhenPaused.value,
                            onCheckedChange = { AppState.setDesktopLyricShowWhenPaused(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppleMusicPink
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Card 2: Selectors (Content & Alignment)
            Text(
                text = "显示设置",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Item 1: Show Content Type
                    Text(
                        text = "显示内容",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        listOf("歌词" to "lyric", "歌名" to "song_name").forEach { (label, value) ->
                            val isSelected = AppState.desktopLyricShowContent.value == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AppleMusicPink else Color.Transparent)
                                    .clickable { AppState.setDesktopLyricShowContent(value) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Item 2: Alignment
                    Text(
                        text = "对齐方式",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        listOf("居中" to "center", "居左" to "left").forEach { (label, value) ->
                            val isSelected = AppState.desktopLyricAlignment.value == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AppleMusicPink else Color.Transparent)
                                    .clickable { AppState.setDesktopLyricAlignment(value) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Card 3: Sliders (Position, Size, Opacity)
            Text(
                text = "位置与样式设置",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Slider 1: X Bias
                    SliderItem(
                        title = "左右位置",
                        value = AppState.desktopLyricX.value,
                        onValueChange = { AppState.setDesktopLyricX(it) },
                        valueRange = 0f..100f
                    ) {
                        when {
                            it < 5f -> "居左"
                            it > 95f -> "居右"
                            else -> "${it.roundToInt()}%"
                        }
                    }

                    // Slider 2: Y Bias
                    SliderItem(
                        title = "上下位置",
                        value = AppState.desktopLyricY.value,
                        onValueChange = { AppState.setDesktopLyricY(it) },
                        valueRange = 0f..100f
                    ) {
                        "${it.roundToInt()}%"
                    }

                    // Slider 3: Width
                    SliderItem(
                        title = "宽度",
                        value = AppState.desktopLyricWidth.value,
                        onValueChange = { AppState.setDesktopLyricWidth(it) },
                        valueRange = 50f..100f
                    ) {
                        "${it.roundToInt()}%"
                    }

                    // Slider 4: Opacity
                    SliderItem(
                        title = "透明度",
                        value = AppState.desktopLyricOpacity.value,
                        onValueChange = { AppState.setDesktopLyricOpacity(it) },
                        valueRange = 10f..100f
                    ) {
                        "${it.roundToInt()}%"
                    }

                    // Slider 5: FontSize
                    SliderItem(
                        title = "字号",
                        value = AppState.desktopLyricFontSize.value,
                        onValueChange = { AppState.setDesktopLyricFontSize(it) },
                        valueRange = 12f..36f
                    ) {
                        "${it.roundToInt()}sp"
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 4: Preset Colors
            Text(
                text = "歌词高亮颜色",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppState.PRESET_COLORS.forEach { (name, color) ->
                            val isSelected = AppState.desktopLyricColorName.value == name
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) AppleMusicPink else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { AppState.setDesktopLyricColorName(name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormatter: (Float) -> String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = valueFormatter(value), color = AppleMusicPink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AppleMusicPink,
                inactiveTrackColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
