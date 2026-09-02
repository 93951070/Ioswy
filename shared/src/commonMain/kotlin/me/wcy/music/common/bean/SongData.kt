package me.wcy.music.common.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SongData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("pst")
    val pst: Int = 0,
    @SerialName("t")
    val t: Int = 0,
    @SerialName("ar")
    val ar: List<ArtistData> = listOf(),
    @SerialName("pop")
    val pop: Int = 0,
    @SerialName("st")
    val st: Int = 0,
    @SerialName("rt")
    val rt: String = "",
    @SerialName("fee")
    val fee: Int = 0,
    @SerialName("v")
    val v: Int = 0,
    @SerialName("cf")
    val cf: String = "",
    @SerialName("al")
    val al: AlbumData = AlbumData(),
    @SerialName("dt")
    val dt: Long = 0,
    @SerialName("h")
    val h: QualityData = QualityData(),
    @SerialName("m")
    val m: QualityData = QualityData(),
    @SerialName("l")
    val l: QualityData = QualityData(),
    @SerialName("sq")
    val sq: QualityData = QualityData(),
    @SerialName("hr")
    val hr: QualityData = QualityData(),
    @SerialName("cd")
    val cd: String = "",
    @SerialName("no")
    val no: Int = 0,
    @SerialName("ftype")
    val ftype: Int = 0,
    @SerialName("djId")
    val djId: Long = 0,
    @SerialName("copyright")
    val copyright: Int = 0,
    @SerialName("s_id")
    val sId: Long = 0,
    @SerialName("mark")
    val mark: Long = 0,
    @SerialName("originCoverType")
    val originCoverType: Int = 0,
    @SerialName("originSongSimpleData")
    val originSongSimpleData: OriginSongSimpleData? = null,
    @SerialName("resourceState")
    val resourceState: Boolean = false,
    @SerialName("version")
    val version: Int = 0,
    @SerialName("single")
    val single: Int = 0,
    @SerialName("rtype")
    val rtype: Int = 0,
    @SerialName("mst")
    val mst: Int = 0,
    @SerialName("cp")
    val cp: Int = 0,
    @SerialName("mv")
    val mv: Long = 0,
    @SerialName("publishTime")
    val publishTime: Long = 0,
    @SerialName("reason")
    val reason: String = "",
    @SerialName("tns")
    val tns: List<String> = listOf(),
    @SerialName("recommendReason")
    val recommendReason: String = "",
    @SerialName("alg")
    val alg: String = ""
)