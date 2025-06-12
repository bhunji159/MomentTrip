import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenttrip.data.model.PlaceData
import com.example.momenttrip.data.model.PlaceSearchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val api = RetrofitClient.create()

    private val _searchResults = MutableStateFlow<List<PlaceData>>(emptyList())
    val searchResults: StateFlow<List<PlaceData>> = _searchResults

    fun searchPlaces(query: String) {
        viewModelScope.launch {
            try {
                val response: PlaceSearchResponse = api.searchPlace(query)
                val list = response.places
                _searchResults.value = list
            } catch (e: Exception) {
                Log.e("MapViewModel", "검색 실패: ${e.message}")
            }
        }
    }
}