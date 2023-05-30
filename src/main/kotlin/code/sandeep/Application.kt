package code.sandeep

import code.sandeep.data.user.MongoUserDataSource
import code.sandeep.data.user.User
import code.sandeep.data.user.UserDataSource
import io.ktor.server.application.*
import code.sandeep.plugins.*
import code.sandeep.security.hashing.SHA256HashingService
import code.sandeep.security.token.JwtTokenService
import code.sandeep.security.token.TokenConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val mongoPw = System.getenv("MONGO_PW")
    val dbName = "ktor-jwt-auth"
    val db = KMongo.createClient(
        "mongodb+srv://ktor-app:$mongoPw@ktor-jwt-auth.xjfecnp.mongodb" + ".net/$dbName?retryWrites=true&w=majority"
    ).coroutine.getDatabase(dbName)
    val userDataSource = MongoUserDataSource(db)
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()
    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig)
}
