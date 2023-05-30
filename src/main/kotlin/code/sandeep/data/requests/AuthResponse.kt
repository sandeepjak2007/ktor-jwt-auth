package code.sandeep.data.requests

@kotlinx.serialization.Serializable
data class AuthResponse(
    val token: String
)