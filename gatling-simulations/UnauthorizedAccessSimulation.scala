import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UnauthorizedAccessSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  // Unauthorized access attempts without authentication
  val unauthorizedScenario = scenario("Unauthorized Access")
    .exec(
      http("Access /admin/ - No Auth")
        .get("/admin/")
        .check(status.in(301, 302, 403, 404))
    )
    .pause(1)
    
    .exec(
      http("Access /manage - No Auth")
        .get("/manage")
        .check(status.in(301, 302, 403, 404))
    )
    .pause(1)
    
    .exec(
      http("Access Job Config - No Auth")
        .get("/job/test/configure")
        .check(status.in(302, 403))
    )
    .pause(1)
    
    .repeat(5) { i =>
      exec(
        http("Access /job/*/configSubmit - No Auth #{index}")
          .post("/job/sensitive-job-#{index}/configSubmit")
          .formParam("script", "malicious code")
          .check(status.in(302, 403, 404))
      )
      .pause(500 milliseconds)
    }
    
    .exec(
      http("Token Tampering - Invalid Token")
        .get("/job/test/")
        .header("X-CSRF-TOKEN", "invalid-token-#{randomInt(99999)}")
        .check(status.in(200, 403))
    )
    .pause(1)
    
    .repeat(10) { i =>
      exec(
        http("Account Enumeration #{index}")
          .post("/j_spring_security_check")
          .formParam("j_username", "user-#{index}@test.com")
          .formParam("j_password", "wrong-password")
          .check(
            status.in(200, 302, 403),
            regex("(?:Invalid|user not found|not found|not exist)").optional
          )
      )
      .pause(200 milliseconds)
    }

  setUp(
    unauthorizedScenario.inject(
      rampUsers(15) during (30 seconds)
    )
  ).protocols(httpProtocol)
}
