import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class NormalUsageSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:147.0) Gecko/20100101 Firefox/147.0")
    .shareConnections

  // Normal user journey - baseline behavior
  val normalUserScenario = scenario("Normal User - Jenkins Admin")
    .exec(
      http("Homepage")
        .get("/")
        .check(status.is(200), status.not(403), status.not(401))
    )
    .pause(1)
    
    .exec(
      http("View Plugin Manager")
        .get("/pluginManager/plugins")
        .check(status.is(200))
    )
    .pause(2)
    
    .exec(
      http("Check Update Center Status")
        .get("/updateCenter/connectionStatus")
        .check(status.is(200))
    )
    .pause(1)
    
    .exec(
      http("Create New Job Page")
        .get("/newJob")
        .check(status.is(200))
    )
    .pause(2)
    
    .exec(
      http("Validate Job Name")
        .get("/checkJobName?value=test-job-#{randomInt(1000)}")
        .check(status.is(200))
    )
    .pause(1)
    
    .exec(
      http("View Dashboard API")
        .get("/api/json")
        .check(status.is(200))
    )
    .pause(2)
    
    .repeat(3) {
      pause(5)
        .exec(
          http("Poll Build Queue")
            .post("/widget/BuildQueueWidget/ajax")
            .check(status.is(200))
        )
        .pause(1, 3)
        .exec(
          http("Poll Executors")
            .post("/widget/ExecutorsWidget/ajax")
            .check(status.is(200))
        )
    }

  setUp(
    normalUserScenario.inject(
      rampUsers(10) during (30 seconds),
      constantUsersPerSec(5) during (60 seconds)
    )
  ).protocols(httpProtocol)
}
