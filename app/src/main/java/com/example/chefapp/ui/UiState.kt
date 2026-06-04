// Puedes ponerlo en com.example.chefapp.ui.UiState
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
    object NoConnection : UiState<Nothing>()
    object SessionExpired : UiState<Nothing>()
}