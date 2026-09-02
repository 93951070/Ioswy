package me.wcy.music.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.wcy.music.account.bean.LoginResultData
import me.wcy.music.account.login.qrcode.QrcodeLoginViewModel
import me.wcy.music.compose.theme.AppThemeColor

@Composable
fun QrcodeLoginScreen(
    viewModel: QrcodeLoginViewModel,
    qrCodeImage: ImageBitmap?,
    onLoginSuccess: () -> Unit = {},
    onSwitchPhone: () -> Unit,
    onMessage: (String) -> Unit
) {
    val status by viewModel.loginStatus.collectAsState()
    var handledSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getLoginQrCode()
    }

    LaunchedEffect(status) {
        if (status?.code == LoginResultData.STATUS_SUCCESS && !handledSuccess) {
            handledSuccess = true
            val res = viewModel.loginWithCookie(status!!.cookie)
            if (res.isSuccessWithData()) {
                onMessage("登录成功")
                onLoginSuccess()
            } else {
                handledSuccess = false
            }
        }
    }

    fun reload() {
        viewModel.getLoginQrCode()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "扫码登录", color = AppThemeColor.TextH1, fontSize = 22.sp)
        qrCodeImage?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "二维码",
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(200.dp)
                    .background(AppThemeColor.ThemeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            )
        }
        when (status?.code) {
            LoginResultData.STATUS_SCANNING -> {
                Text(
                    text = "「${status?.nickname}」授权中",
                    color = AppThemeColor.TextH2,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            LoginResultData.STATUS_INVALID -> {
                StatusText("二维码已失效，点击刷新", onTextClick = { reload() })
            }
            LoginResultData.STATUS_SUCCESS -> {
                StatusText(status?.message ?: "登录成功")
            }
            else -> {
                if (status != null && status?.code != LoginResultData.STATUS_NOT_SCAN && qrCodeImage != null) {
                    StatusText(status?.message?.ifEmpty { "二维码错误，点击刷新" } ?: "二维码错误，点击刷新", onTextClick = { reload() })
                } else if (status == null) {
                    StatusText("加载中…")
                }
            }
        }
        Text(
            text = "手机号登录",
            color = AppThemeColor.ThemeColor,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable(onClick = onSwitchPhone)
        )
    }
}

@Composable
private fun StatusText(text: String, onTextClick: (() -> Unit)? = null) {
    Text(
        text = text,
        color = AppThemeColor.TextH2,
        fontSize = 14.sp,
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable(enabled = onTextClick != null) { onTextClick?.invoke() }
    )
}
