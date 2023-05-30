package code.sandeep.security.token

fun interface TokenService {
    fun generate(
        config: TokenConfig, vararg claims: TokenClaim
    ): String
}