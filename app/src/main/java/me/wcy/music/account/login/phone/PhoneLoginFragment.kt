package me.wcy.music.account.login.phone

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.account.login.LoginRouteFragment
import me.wcy.music.common.BaseMusicFragment
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.PhoneLoginScreen
import me.wcy.music.consts.RoutePath
import me.wcy.music.shared.account.UserSession
import me.wcy.router.annotation.Route
import top.wangchenyan.common.utils.ToastUtils
import javax.inject.Inject

/**
 * Created by wangchenyan.top on 2024/1/3.
 */
@Route(RoutePath.PHONE_LOGIN)
@AndroidEntryPoint
class PhoneLoginFragment : BaseMusicFragment() {
    private val viewModel by viewModels<PhoneLoginViewModel> {
        viewModelFactory {
            initializer {
                PhoneLoginViewModel(userSession)
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
                    PhoneLoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { setResultAndFinish() },
                        onSwitchQrcode = {
                            activity?.apply {
                                setResult(LoginRouteFragment.RESULT_SWITCH_QRCODE)
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
