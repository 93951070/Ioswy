package me.wcy.music.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.wcy.music.account.login.phone.PhoneLoginViewModel
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun PhoneLoginScreen(
    viewModel: PhoneLoginViewModel,
    onLoginSuccess: () -> Unit,
    onSwitchQrcode: () -> Unit,
    onMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val countdown by viewModel.sendPhoneCodeCountdown.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "手机号登录",
            color = AppThemeColor.TextH1,
            fontSize = 22.sp
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("手机号") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("手机验证码") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    if (phone.isNullOrEmpty()) {
                        onMessage("请输入手机号")
                    } else {
                        scope.launch {
                            val res = viewModel.sendPhoneCode(phone)
                            if (res.isSuccess().not()) {
                                onMessage(res.msg ?: "")
                            }
                        }
                    }
                },
                enabled = countdown == 0
            ) {
                Text(
                    text = if (countdown > 0) "${countdown}秒后重发" else "获取验证码",
                    color = if (countdown > 0) AppThemeColor.TextH2 else AppThemeColor.ThemeColor
                )
            }
        }
        Button(
            onClick = {
                if (phone.isNullOrEmpty()) {
                    onMessage("请输入手机号")
                } else if (code.isNullOrEmpty()) {
                    onMessage("请输入手机验证码")
                } else {
                    scope.launch {
                        val res = viewModel.phoneLogin(phone, code)
                        if (res.isSuccess()) {
                            onMessage("登录成功")
                            onLoginSuccess()
                        } else {
                            onMessage(res.msg.orEmpty().ifEmpty { "登录失败，请更新服务端版本或稍后重试" })
                        }
                    }
                }
            },
            enabled = phone.isNotEmpty() && code.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = AppThemeColor.ThemeColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("登录", fontSize = 16.sp)
        }
        Text(
            text = "二维码登录",
            color = AppThemeColor.ThemeColor,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(onClick = onSwitchQrcode)
        )
    }
}
