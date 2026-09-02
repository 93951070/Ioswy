package me.wcy.music.discover.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.wcy.music.account.bean.ProfileData
import me.wcy.music.common.bean.PlaylistData
import me.wcy.music.discover.banner.BannerData
import me.wcy.music.shared.net.DiscoverNet

interface DiscoverCacheStore {
    suspend fun getBanners(): List<BannerData>?
    suspend fun putBanners(value: List<BannerData>)
    suspend fun getRecommendPlaylists(): List<PlaylistData>?
    suspend fun putRecommendPlaylists(value: List<PlaylistData>)
    suspend fun getRankingList(): List<PlaylistData>?
    suspend fun putRankingList(value: List<PlaylistData>)
}

class DiscoverViewModel(
    private val profileFlow: StateFlow<ProfileData?>,
    private val hasApiDomain: () -> Boolean,
    private val cache: DiscoverCacheStore? = null
) : ViewModel() {
    private val _bannerList = MutableStateFlow<List<BannerData>>(emptyList())
    val bannerList = _bannerList.asStateFlow()

    private val _recommendPlaylist = MutableStateFlow<List<PlaylistData>>(emptyList())
    val recommendPlaylist = _recommendPlaylist.asStateFlow()

    private val _rankingList = MutableStateFlow<List<PlaylistData>>(emptyList())
    val rankingList = _rankingList.asStateFlow()

    init {
        loadCache()
        viewModelScope.launch {
            profileFlow.collectLatest { profile ->
                if (profile != null && hasApiDomain()) {
                    loadRecommendPlaylist()
                }
            }
        }
        loadBanner()
        loadRankingList()
    }

    /** 接口域名变更后由壳层调用，强制重新拉取发现页数据 */
    fun refresh() {
        loadBanner()
        loadRankingList()
        if (profileFlow.value != null && hasApiDomain()) {
            loadRecommendPlaylist()
        }
    }

    private fun loadCache() {
        viewModelScope.launch {
            val banners = cache?.getBanners() ?: return@launch
            _bannerList.value = banners
        }
        viewModelScope.launch {
            val list = cache?.getRecommendPlaylists() ?: return@launch
            _recommendPlaylist.value = list
        }
        viewModelScope.launch {
            val list = cache?.getRankingList() ?: return@launch
            _rankingList.value = list
        }
    }

    private fun loadBanner() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getBannerList()
            }.onSuccess {
                _bannerList.value = it.banners
                cache?.putBanners(it.banners)
            }
        }
    }

    private fun loadRecommendPlaylist() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getRecommendPlaylists()
            }.onSuccess {
                _recommendPlaylist.value = it.playlists
                cache?.putRecommendPlaylists(it.playlists)
            }
        }
    }

    private fun loadRankingList() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getRankingList()
            }.onSuccess {
                val rankingList = it.playlists.take(5)
                val deferredList = mutableListOf<Deferred<*>>()
                rankingList.forEach {
                    val d = async {
                        val songListRes = kotlin.runCatching {
                            DiscoverNet.getPlaylistSongList(it.id, limit = 3)
                        }
                        if (songListRes.getOrNull()?.code == 200) {
                            it.songList = songListRes.getOrThrow().songs
                        }
                    }
                    deferredList.add(d)
                }
                deferredList.forEach { d ->
                    d.await()
                }
                _rankingList.value = rankingList
                cache?.putRankingList(rankingList)
            }
        }
    }

    companion object {
        const val CACHE_KEY_BANNER = "discover_banner"
        const val CACHE_KEY_REC_PLAYLIST = "discover_recommend_playlist"
        const val CACHE_KEY_RANKING_LIST = "discover_ranking_list"
    }
}
