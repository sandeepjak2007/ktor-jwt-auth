package code.sandeep

import code.sandeep.data.requests.AuthRequest
import code.sandeep.data.requests.AuthResponse
import code.sandeep.data.user.User
import code.sandeep.data.user.UserDataSource
import code.sandeep.security.hashing.HashingService
import code.sandeep.security.hashing.SaltedHash
import code.sandeep.security.token.TokenClaim
import code.sandeep.security.token.TokenConfig
import code.sandeep.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService, userDataSource: UserDataSource
) {
    post("signup") {
        val request = runCatching<AuthRequest?> { call.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        if (areFieldsBlank || isPwTooShort) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        val saltedHash = hashingService.generatedSaltedHash(request.password)
        val user = User(
            userName = request.username, password = saltedHash.hash, salt = saltedHash.salt
        )

        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }

}

fun Route.signIn(
    hashingService: HashingService, userDataSource: UserDataSource, tokenService: TokenService, tokenConfig: TokenConfig
) {
    post("signin") {
        val request = runCatching<AuthRequest?> { call.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val user = userDataSource.getUserByUserName(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "No User Found")
            return@post
        }
        val isValidPassword = hashingService.verify(
            value = request.password, saltedHash = SaltedHash(
                hash = user.password, salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig, TokenClaim(name = "userId", value = user.id.toString())
        )
        call.respond(
            status = HttpStatusCode.OK, message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your user id is $userId")
        }
    }
}