package code.sandeep.plugins

import code.sandeep.authenticate
import code.sandeep.data.user.UserDataSource
import code.sandeep.getSecretInfo
import code.sandeep.security.hashing.HashingService
import code.sandeep.security.token.TokenClaim
import code.sandeep.security.token.TokenConfig
import code.sandeep.security.token.TokenService
import code.sandeep.signIn
import code.sandeep.signUp
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(hashingService,userDataSource, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
    }
}
