package lt.ajasiunas.musicbrainz.network.dto


import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("count")
    val count: Int,
    @SerializedName("created")
    val created: String,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("places")
    val places: List<Place>
)