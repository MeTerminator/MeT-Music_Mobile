package top.met6.music.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.met6.music.mobile.state.AppState

val AppleMusicPink = Color(0xFFFC3C5C)
val DarkBackground = Color(0xFF16161C)
val CardBackground = Color(0xFF24242E)

@Composable
fun LoginScreen() {
    var qqInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C1318),
                        DarkBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
        ) {
            // App Name Logo
            Text(
                text = "MeT-Music",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "用音乐连接彼此",
                fontSize = 14.sp,
                color = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // Input fields card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "使用 QQ 号登录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = qqInput,
                        onValueChange = {
                            qqInput = it
                            isError = false
                            statusMessage = ""
                        },
                        placeholder = { Text("请输入 QQ 号以开始", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AppleMusicPink,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (AppState.userPlaylistsLoading.value) {
                        CircularProgressIndicator(color = AppleMusicPink)
                    } else {
                        Button(
                            onClick = {
                                AppState.login(qqInput) { success, msg ->
                                    isError = !success
                                    statusMessage = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleMusicPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "登 录",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = if (isError) Color.Red else Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}
