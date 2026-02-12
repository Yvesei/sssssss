import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class DenialOfServiceSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  // Slowloris attack - slow HTTP requests
  val slowlorisScenario = scenario("DoS - Slowloris")
    .repeat(10) {
      exec(
        http("Slowloris Request #{index}")
          .get("/")
          .requestTimeout(60000) // 60 second timeout
          .check(status.in(200, 408, 500, 503))
      )
      .pause(5)
    }

  // Massive concurrent requests
  val ddosScenario = scenario("DoS - High Concurrency")
    .repeat(50) { i =>
      exec(
        http("Concurrent Request #{index}")
          .get("/api/json")
          .check(status.in(200, 500, 503))
      )
      .pause(10 milliseconds)
    }

  // Large file/bandwidth exhaustion
  val bandwidthScenario = scenario("DoS - Bandwidth Exhaustion")
    .repeat(5) { i =>
      exec(
        http("Download Large File #{index}")
          .get("/pluginManager/plugins")
          .check(status.is(200))
      )
      .pause(1)
    }

  // CPU exhaustion - complex queries
  val cpuExhaustionScenario = scenario("DoS - CPU Exhaustion")
    .repeat(30) { i =>
      exec(
        http("Complex Query #{index}")
          .get("/api/json?tree=jobs[name,description,lastBuild[result,duration]]")
          .check(status.in(200, 500, 503))
      )
      .pause(100 milliseconds)
    }

  setUp(
    slowlorisScenario.inject(atOnceUsers(5)),
    ddosScenario.inject(atOnceUsers(50)),
    bandwidthScenario.inject(atOnceUsers(10)),
    cpuExhaustionScenario.inject(rampUsers(30) during (10 seconds))
  ).protocols(httpProtocol)
}
