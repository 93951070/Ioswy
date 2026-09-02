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
import me.wcy.music.common.bean.SongData
import me.wcy.music.discover.artist.bean.HotArtistData
import me.wcy.music.discover.banner.BannerData
import me.wcy.music.dj.bean.DjRadioData
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

    private val _dailySongs = MutableStateFlow<List<SongData>>(emptyList())
    val dailySongs = _dailySongs.asStateFlow()

    private val _hotArtistList = MutableStateFlow<List<HotArtistData>>(emptyList())
    val hotArtistList = _hotArtistList.asStateFlow()

    private val _highQualityPlaylists = MutableStateFlow<List<PlaylistData>>(emptyList())
    val highQualityPlaylists = _highQualityPlaylists.asStateFlow()

    private val _djRadioList = MutableStateFlow<List<DjRadioData>>(emptyList())
    val djRadioList = _djRadioList.asStateFlow()

    init {
        loadCache()
        loadDailySongs()
        loadHotArtists()
        loadHighQualityPlaylists()
        loadDjRadios()
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
        loadDailySongs()
        loadHotArtists()
        loadHighQualityPlaylists()
        loadDjRadios()
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

    private fun loadDailySongs() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getRecommendSongs()
            }.onSuccess { result ->
                if (result.isSuccessWithData()) {
                    _dailySongs.value = result.data?.dailySongs.orEmpty().take(10)
                }
            }
        }
    }

    private fun loadHotArtists() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getHotArtistList(limit = 10)
            }.onSuccess {
                _hotArtistList.value = it.artists
            }
        }
    }

    private fun loadHighQualityPlaylists() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getHighQualityPlaylistList(limit = 10)
            }.onSuccess {
                _highQualityPlaylists.value = it.playlists
            }
        }
    }

    private fun loadDjRadios() {
        viewModelScope.launch {
            kotlin.runCatching {
                DiscoverNet.getDjRecommendList(limit = 6)
            }.onSuccess {
                _djRadioList.value = it.djRadios
            }
        }
    }

    companion object {
        const val CACHE_KEY_BANNER = "discover_banner"
        const val CACHE_KEY_REC_PLAYLIST = "discover_recommend_playlist"
        const val CACHE_KEY_RANKING_LIST = "discover_ranking_list"
    }
}
