package lt.ajasiunas.musicbrainz.ui.maps

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import lt.ajasiunas.musicbrainz.R
import lt.ajasiunas.musicbrainz.network.PLACES_SEARCH_LIMIT
import lt.ajasiunas.musicbrainz.network.PlacesService
import lt.ajasiunas.musicbrainz.network.dto.Place
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MapsViewModel @Inject constructor(private val placesService: PlacesService) : ViewModel() {
    companion object {
        const val DELAY_LIFE_TIME = 31L * 1000 // 2021-1990
    }

    private lateinit var uiPinCallback: suspend (List<Pin>) -> Unit

    /***********
     * UI
     ***********/

    private val _totalPlaces = MutableLiveData<Int>(0)
    val totalPlaces: LiveData<Int> get() = _totalPlaces

    private val _totalPlacesReceived = MutableLiveData<Int>(0)
    val totalPlacesReceived: LiveData<Int> get() = _totalPlacesReceived

    private val _totalUnsuccessfulRequests = MutableLiveData<Int>()
    val totalUnsuccessfulRequests: LiveData<Int> get() = _totalUnsuccessfulRequests

    private val _countdownSeconds = MutableLiveData<Int>()
    val countdownSeconds: LiveData<Int> get() =_countdownSeconds

    val startCountdown = MutableLiveData<Boolean>()

    fun setUiPinCallback(callback: suspend (List<Pin>) -> Unit) {
        uiPinCallback = callback
    }

    val countDownTimer = object : CountDownTimer(DELAY_LIFE_TIME, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
            _countdownSeconds.postValue((millisUntilFinished / 1000).toInt())
        }

        override fun onFinish() {
            reset()
        }

    }

    fun startCountdown() {
        countDownTimer.start()
        startCountdown.postValue(true)
    }


    /***********
     * Network
     **********/

    fun requestPlaces(query: String) {
        viewModelScope.launch {
            val errorCount = AtomicInteger(0)

            try {
                val response = placesService.search(query)
                _totalPlaces.postValue(response.count)

                withContext(Dispatchers.IO) {
                    getPlaces(query, response.count)
                    startCountdown()
                }
            } catch (e: Exception) {
                _totalUnsuccessfulRequests.postValue(errorCount.getAndIncrement())
            }
        }
    }

    suspend fun getPlaces(query: String, amountPlacesToGet: Int) =
        coroutineScope {
            var placesReceived = 0
            val offset = AtomicInteger(0)
            val delay = AtomicLong(0)

            var loopCount = amountPlacesToGet / PLACES_SEARCH_LIMIT
            if (amountPlacesToGet % PLACES_SEARCH_LIMIT > 0) {
                loopCount++
            }

            val channel = Channel<List<Place>>()
            repeat(loopCount) {
                launch {
                    delay(delay.getAndAdd(1000)) // cause throttling

                    val response = placesService.search(
                        query, offset.getAndAdd(PLACES_SEARCH_LIMIT)
                    )
                    channel.send(response.places)
                }
            }

            repeat(loopCount) {
                val places = channel.receive()
                placesReceived += places.size

                uiPinCallback(places.mapToPins())
                _totalPlacesReceived.postValue(placesReceived)
            }
        }

    /***********
     * Helpers
     ***********/

    private fun reset() {
        // reset texts!
        _totalUnsuccessfulRequests.postValue(0)
        _totalPlaces.postValue(0)
        _totalPlacesReceived.postValue(0)
        startCountdown.postValue(false)
    }

    private fun List<Place>.mapToPins(): List<Pin> {
        return filter { it.lifeSpan.begin != null && it.coordinates != null }
            .map {
                Pin(
                    title = it.name,
                    latLng = LatLng(
                        it.coordinates!!.latitude.toDouble(),
                        it.coordinates.longitude.toDouble()
                    ),
                    begin = it.lifeSpan.begin!!.take(4).toInt(),
                    end = it.lifeSpan.end?.take(4)?.toInt() ?: Calendar.getInstance()
                        .get(Calendar.YEAR)
                )
            }
    }
}