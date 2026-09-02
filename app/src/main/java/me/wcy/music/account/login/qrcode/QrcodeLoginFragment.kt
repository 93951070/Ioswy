package me.wcy.music.account.login.qrcode

import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cn.bertsir.zbar.utils.QRUtils
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.account.login.LoginRouteFragment
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.QrcodeLoginScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.shared.account.UserSession
import me.wcy.router.annotation.Route
import top.wangchenyan.common.utils.ToastUtils
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2023/8/28.
 */
@Route(RoutePath.QRCODE_LOGIN)
@AndroidEntryPoint
class QrcodeLoginFragment : BaseMusicFragment() {
    private val viewModel by viewModels<QrcodeLoginViewModel> {
        viewModelFactory {
            initializer {
                QrcodeLoginViewModel(userSession)
            }
        }
    }
    private var composeView: ComposeView? = null

    @Inject
    lateinit var userSession: UserSession

    override fun getRootView(): View {
        return composeView ?: ComposeView(requireContext()).also { view ->
            view.setContent {
                MusicTheme {
                    val qrUrl by viewModel.qrUrl.collectAsState()
                    val qrCodeImage = qrUrl?.let { url ->
                        QRUtils.getInstance().createQRCode(url)?.asImageBitmap()
                    }
                    QrcodeLoginScreen(
                        viewModel = viewModel,
                        qrCodeImage = qrCodeImage,
                        onLoginSuccess = { setResultAndFinish() },
                        onBack = { activity?.finish() },
                        onSwitchPhone = {
                            activity?.apply {
                                setResult(LoginRouteFragment.RESULT_SWITCH_PHONE)
                                finish()
                            }
                        },
                        onMessage = { ToastUtils.show(it) }
                    )
                }
            }
            composeView = view
        }
    }

    override fun isLazy(): Boolean {
        return false
    }
}
