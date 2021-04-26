package lt.ajasiunas.musicbrainz.network.dto


import com.google.gson.annotations.SerializedName

data class LifeSpan(
    @SerializedName("ended")
    val ended: Any
)