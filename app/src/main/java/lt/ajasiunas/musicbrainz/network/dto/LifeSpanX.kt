package lt.ajasiunas.musicbrainz.network.dto


import com.google.gson.annotations.SerializedName

data class LifeSpanX(
    @SerializedName("begin")
    val begin: String?,
    @SerializedName("end")
    val end: String?,
    @SerializedName("ended")
    val ended: Boolean?
)