package me.wcy.music.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import me.wcy.music.R
import me.wcy.music.common.BaseMusicActivity
import me.wcy.music.common.DarkModeService
import me.wcy.music.compose.theme.MusicTheme
import me.wcy.music.compose.ui.SettingChoice
import me.wcy.music.compose.ui.SettingItem
import me.wcy.music.compose.ui.SettingsScreen
import me.wcy.music.service.PlayerController
import me.wcy.music.storage.preference.ConfigPreferences
import me.wcy.music.utils.MusicUtils
import me.wcy.router.annotation.Route
import top.wangchenyan.common.ext.toast
import javax.inject.Inject

@Route("/settings")
@AndroidEntryPoint
class SettingsActivity : BaseMusicActivity() {

    @Inject
    lateinit var playerController: PlayerController

    @Inject
    lateinit var darkModeService: DarkModeService

    private var items by mutableStateOf<List<SettingItem>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        items = buildItems()
        setContent {
            MusicTheme {
                SettingsScreen(
                    items = items,
                    onItemChange = ::onItemChange,
                    onOpenSoundEffect = ::startEqualizer,
                    onBack = { finish() }
                )
            }
        }
    }

    private fun onItemChange(key: String, value: String) {
        when (key) {
            KEY_DARK_MODE -> {
                ConfigPreferences.darkMode = value
                darkModeService.setDarkMode(DarkModeService.DarkMode.fromValue(value))
            }
            KEY_PLAY_SOUND_QUALITY -> ConfigPreferences.playSoundQuality = value
            KEY_DOWNLOAD_SOUND_QUALITY -> ConfigPreferences.downloadSoundQuality = value
            KEY_FILTER_SIZE -> ConfigPreferences.filterSize = value
            KEY_FILTER_TIME -> ConfigPreferences.filterTime = value
        }
        items = items.map {
            if (it.key == key) {
                it.copy(value = it.options.firstOrNull { o -> o.value == value }?.label ?: value)
            } else {
                it
            }
        }
    }

    private fun buildItems(): List<SettingItem> = listOf(
        SettingItem(
            key = KEY_DARK_MODE,
            category = "通用",
            title = "外观",
            dialogTitle = "外观",
            value = ConfigPreferences.darkMode,
            options = choices(R.array.dark_mode_entries, R.array.dark_mode_values)
        ),
        SettingItem(
            key = KEY_PLAY_SOUND_QUALITY,
            category = "播放",
            title = "在线播放音质",
            dialogTitle = "🇻需要VIP 🇸需要SVIP",
            value = ConfigPreferences.playSoundQuality,
            options = soundQualityChoices()
        ),
        SettingItem(
            key = KEY_SOUND_EFFECT,
            category = "播放",
            title = getString(R.string.sound_effect),
            dialogTitle = "",
            value = "",
            options = emptyList()
        ),
        SettingItem(
            key = KEY_DOWNLOAD_SOUND_QUALITY,
            category = "下载",
            title = "下载音质",
            dialogTitle = "🇻需要VIP 🇸需要SVIP",
            value = ConfigPreferences.downloadSoundQuality,
            options = soundQualityChoices()
        ),
        SettingItem(
            key = KEY_FILTER_SIZE,
            category = "文件过滤",
            title = "按大小过滤",
            dialogTitle = "最小大小",
            value = ConfigPreferences.filterSize,
            options = choices(R.array.filter_size_entries, R.array.filter_size_entry_values)
        ),
        SettingItem(
            key = KEY_FILTER_TIME,
            category = "文件过滤",
            title = "按时长过滤",
            dialogTitle = "最小时长",
            value = ConfigPreferences.filterTime,
            options = choices(R.array.filter_time_entries, R.array.filter_time_entry_values)
        )
    )

    private fun soundQualityChoices() = choices(
        R.array.sound_quality_entries,
        R.array.sound_quality_entry_values
    )

    private fun choices(entriesRes: Int, valuesRes: Int): List<SettingChoice> {
        val entries = resources.getStringArray(entriesRes)
        val values = resources.getStringArray(valuesRes)
        return entries.mapIndexed { index, label -> SettingChoice(label, values[index]) }
    }

    private fun startEqualizer() {
        if (MusicUtils.isAudioControlPanelAvailable(this)) {
            val intent = Intent()
            intent.action = AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            intent.putExtra(
                AudioEffect.EXTRA_AUDIO_SESSION,
                playerController.getAudioSessionId()
            )
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                toast(R.string.device_not_support)
            }
        } else {
            toast(R.string.device_not_support)
        }
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PLAY_SOUND_QUALITY = "play_sound_quality"
        private const val KEY_SOUND_EFFECT = "sound_effect"
        private const val KEY_DOWNLOAD_SOUND_QUALITY = "download_sound_quality"
        private const val KEY_FILTER_SIZE = "filter_size"
        private const val KEY_FILTER_TIME = "filter_time"
    }
}
