package top.met6.music.mobile.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.met6.music.mobile.state.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    // Refresh cache sizes when entering Settings
    LaunchedEffect(Unit) {
        AppState.updateCacheSizes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "设置") },
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
            // User Profile Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    val avatar = AppState.avatarUrl.value
                    if (avatar.isNotEmpty()) {
                        CachedImage(
                            songId = "user_avatar",
                            imageUrl = avatar,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                            .background(Color.Gray)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppState.nickname.value,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "QQ ID: ${AppState.qqId.value}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    
                    Button(
                        onClick = { AppState.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMusicPink),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(text = "登出", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sound Quality Section
            Text(
                text = "音质选择",
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
                Column(modifier = Modifier.padding(8.dp)) {
                    val qualities = listOf(
                        "standard" to "标准 (Standard)",
                        "higher" to "较高 (Higher)",
                        "exhigh" to "极高 (ExHigh)",
                        "lossless" to "无损 (Lossless)",
                        "hires" to "Hi-Res (Hi-Res)"
                    )
                    
                    qualities.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { AppState.setSoundQualityLevel(key) }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = AppState.soundQuality.value == key,
                                onClick = { AppState.setSoundQualityLevel(key) },
                                colors = RadioButtonDefaults.colors(selectedColor = AppleMusicPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Cache Settings Section
            Text(
                text = "缓存管理",
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
                    CacheRow(
                        title = "歌单数据缓存",
                        sizeMb = AppState.playlistCacheSize.value
                    ) {
                        AppState.clearCacheCategory("playlists")
                    }
                    
                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    
                    CacheRow(
                        title = "封面图片缓存",
                        sizeMb = AppState.coverCacheSize.value
                    ) {
                        AppState.clearCacheCategory("covers")
                    }
                    
                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    
                    CacheRow(
                        title = "音频文件缓存",
                        sizeMb = AppState.songCacheSize.value
                    ) {
                        AppState.clearCacheCategory("songs")
                    }
                }
            }
        }
    }
}

@Composable
fun CacheRow(title: String, sizeMb: Double, onClear: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            val integerPart = sizeMb.toLong()
            val fractionalPart = ((sizeMb - integerPart) * 100).toLong()
            val fracStr = if (fractionalPart < 0) -fractionalPart else fractionalPart
            val fracPad = if (fracStr < 10) "0$fracStr" else "$fracStr"
            Text(text = "$integerPart.$fracPad MB", color = Color.Gray, fontSize = 12.sp)
        }
        
        OutlinedButton(
            onClick = onClear,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleMusicPink),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AppleMusicPink)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(text = "清除", fontSize = 13.sp)
        }
    }
}
