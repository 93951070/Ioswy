package me.wcy.music.shared.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.wcy.music.common.bean.SharedJson
import me.wcy.music.common.bean.decodeBean

@Serializable
data class SearchDefaultData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: SearchDefaultInner? = null,
)

@Serializable
data class SearchDefaultInner(
    @SerialName("showKeyword")
    val showKeyword: String = "",
    @SerialName("realkeyword")
    val realKeyword: String = "",
    @SerialName("searchType")
    val searchType: Int = 0,
    @SerialName("alg")
    val alg: String = "",
)

@Serializable
data class SearchSuggestData(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("result")
    val result: SearchSuggestResult? = null,
)

@Serializable
data class SearchSuggestResult(
    @SerialName("allMatch")
    val allMatch: List<SearchSuggestItem> = emptyList(),
)

@Serializable
data class SearchSuggestItem(
    @SerialName("keyword")
    val keyword: String = "",
    @SerialName("type")
    val type: Int = 0,
    @SerialName("alg")
    val alg: String = "",
    @SerialName("lastKeyword")
    val lastKeyword: String = "",
    @SerialName("feature")
    val feature: String = "",
)

/**
 * 搜索增强接口：默认搜索词、搜索联想。
 */
object SearchExtraNet {

    /**
     * 默认搜索词（无关键词时搜索框占位内容）。
     * showKeyword 为展示文案（可能带 emoji 装饰），realKeyword 为真实搜索词。
     */
    suspend fun getDefaultSearch(keywords: String): SearchDefaultData {
        return SharedJson.decodeBean(SharedNet.get(
                "search/default",
                params = listOf("keywords" to keywords)
            )
        )
    }

    /**
     * 搜索联想词。type 固定 mobile，返回 allMatch 匹配项数组。
     */
    suspend fun getSearchSuggest(
        keywords: String,
        type: String = "mobile",
    ): SearchSuggestData {
        return SharedJson.decodeBean(SharedNet.get(
                "search/suggest",
                params = listOf(
                    "keywords" to keywords,
                    "type" to type
                )
            )
        )
    }
}
