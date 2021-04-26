package lt.ajasiunas.musicbrainz.network

sealed class Result<T>(val data: T? = null) {
    class Success<T>(data: T) : Result<T>(data)
    class Loading<T>() : Result<T>()
    class Error<T>(val message: String) : Result<T>()
}