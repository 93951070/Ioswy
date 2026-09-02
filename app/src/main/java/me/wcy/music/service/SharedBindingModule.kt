package me.wcy.music.service

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.wcy.music.discover.comment.MyCommentStore
import me.wcy.music.discover.comment.MyCommentStoreImpl
import me.wcy.music.shared.account.UserSession

@Module
@InstallIn(SingletonComponent::class)
abstract class SharedBindingModule {

    @Binds
    abstract fun bindUserSession(userSessionBridge: UserSessionBridge): UserSession
}

@Module
@InstallIn(SingletonComponent::class)
object MyCommentStoreModule {

    @Provides
    fun provideMyCommentStore(): MyCommentStore = MyCommentStoreImpl
}
