import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class APIAbuseSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  // Rapid job creation/deletion
  val jobAbuseScenario = scenario("API Abuse - Rapid Job Operations")
    .repeat(50) { i =>
      exec(
        http("Create Job #{index}")
          .post("/createItem?name=spam-job-#{index}-#{randomInt(99999)}")
          .formParam("type", "org.jenkinsci.plugins.workflow.job.WorkflowJob")
          .check(status.in(200, 302, 400, 403))
      )
      .pause(100 milliseconds)
      .exec(
        http("Delete Job #{index}")
          .post("/job/spam-job-#{index}-#{randomInt(99999)}/doDelete")
          .check(status.in(200, 302, 400, 403, 404))
      )
      .pause(100 milliseconds)
    }

  // Excessive API queries
  val apiQueryScenario = scenario("API Abuse - Excessive Queries")
    .repeat(100) {
      exec(
        http("API Query #{index}")
          .get("/api/json?tree=jobs[name]")
          .check(status.is(200))
      )
      .pause(50 milliseconds)
    }

  // Large payload submission
  val largePayloadScenario = scenario("API Abuse - Large Payloads")
    .repeat(10) { i =>
      exec(
        http("Large Payload #{index}")
          .post("/job/test/configSubmit")
          .body(StringBody(
            "script=" + ("A" * 50000)
          ))
          .check(status.in(200, 302, 400, 403))
      )
      .pause(1)
    }

  setUp(
    jobAbuseScenario.inject(atOnceUsers(10)),
    apiQueryScenario.inject(atOnceUsers(20)),
    largePayloadScenario.inject(atOnceUsers(5))
  ).protocols(httpProtocol)
}
