package lt.ajasiunas.musicbrainz.ui.maps

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap

import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import lt.ajasiunas.musicbrainz.R
import lt.ajasiunas.musicbrainz.databinding.FragmentMapsBinding
import kotlin.random.Random

@AndroidEntryPoint
class MapsFragment : Fragment() {
    private val viewModel: MapsViewModel by viewModels()
    private lateinit var binding: FragmentMapsBinding
    private val markers = mutableListOf<Marker>()

    lateinit var map: GoogleMap
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addObservers()
        binding = FragmentMapsBinding.inflate(layoutInflater)
        binding.mapsViewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.setUiPinCallback(updatePinsCallback())
        setupSearch()
        return binding.root
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.requestPlaces(query)
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?) = true
        })
    }

    private fun updatePinsCallback(): suspend (List<Pin>) -> Unit {
        return { pins ->
            withContext(Dispatchers.Main) {
                markers += pins.map {
                    map.addMarker(
                        MarkerOptions()
                            .position(it.latLng)
                            .title(it.title))
                }
            }
        }

    }

    private fun addObservers() {
        viewModel.totalPlaces.observe(viewLifecycleOwner) {
            binding.tvPlacesTotal.text = getString(R.string.total_places_format, it)
        }

        viewModel.totalPlacesReceived.observe(viewLifecycleOwner) {
            binding.tvPlacesReturned.text = getString(R.string.places_returned_format, it)
        }

        viewModel.totalUnsuccessfulRequests.observe(viewLifecycleOwner) {
            binding.tvPlacesErrors.text = getString(R.string.failed_requests_format, it)
        }

        viewModel.startCountdown.observe(viewLifecycleOwner) { isDone ->
            if(isDone) {
                Snackbar.make(requireView(), "Done loading", Snackbar.LENGTH_LONG).show()
                markers.forEach { marker ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(Random.nextLong(MapsViewModel.DELAY_LIFE_TIME))
                        marker.remove()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

    }
}