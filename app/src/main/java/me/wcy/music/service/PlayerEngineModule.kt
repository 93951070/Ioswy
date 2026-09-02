package me.wcy.music.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.wcy.music.shared.player.PlayerEngine

/**
 * PlayerEngine 接口绑定：shared UI 层通过接口注入，宿主实现为 PlayerEngineBridge。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerEngineModule {

    @Binds
    abstract fun bindPlayerEngine(playerEngineBridge: PlayerEngineBridge): PlayerEngine
}
