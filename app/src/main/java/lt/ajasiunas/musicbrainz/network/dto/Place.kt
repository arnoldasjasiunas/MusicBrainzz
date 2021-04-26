package lt.ajasiunas.musicbrainz.network.dto


import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("address")
    val address: String,
    @SerializedName("aliases")
    val aliases: List<Aliase>,
    @SerializedName("area")
    val area: Area,
    @SerializedName("coordinates")
    val coordinates: Coordinates?,
    @SerializedName("disambiguation")
    val disambiguation: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("life-span")
    val lifeSpan: LifeSpanX,
    @SerializedName("name")
    val name: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("type-id")
    val typeId: String
)