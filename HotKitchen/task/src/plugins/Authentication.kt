package hotkitchen.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.user.UserType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import java.util.*

fun Application.configureAuthentication() = run {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()
    install(Authentication) {
        jwt {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build())
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.generateToken(claims: List<Pair<String, String>>): String {
    val secret = call.application.environment.config.property("jwt.secret").getString()
    val issuer = call.application.environment.config.property("jwt.issuer").getString()
    val audience = call.application.environment.config.property("jwt.audience").getString()

    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer).apply {
            claims.forEach { this.withClaim(it.first, it.second) }
        }
        .withExpiresAt(Date(System.currentTimeMillis() + 300_000)) // 5min
        .sign(Algorithm.HMAC256(secret))
}

fun PipelineContext<Unit, ApplicationCall>.checkForUserType(requiredType: UserType) {
    val principal = call.principal<JWTPrincipal>()
    val userType = UserType.from(principal!!.payload.getClaim("userType").asString())

    require(userType != null) { "User has no valid type" }
    require(userType == requiredType) { "User must have type $requiredType" }
}

