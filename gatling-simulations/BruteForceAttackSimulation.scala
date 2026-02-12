import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BruteForceAttackSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  val credentials = csv("credentials.csv").records

  // Brute force - rapid login attempts with different credentials
  val bruteForceScenario = scenario("Brute Force Attack")
    .feed(credentials)
    .repeat(20) {
      exec(
        http("Login Attempt - #{username}")
          .post("/j_spring_security_check")
          .formParam("j_username", "#{username}")
          .formParam("j_password", "#{password}")
          .check(
            status.in(200, 302, 403),
            regex("(?:Invalid|Incorrect|Authentication|Failed)").optional
          )
      )
      .pause(100 milliseconds, 500 milliseconds)
    }

  setUp(
    bruteForceScenario.inject(
      atOnceUsers(20)
    )
  ).protocols(httpProtocol)
}
