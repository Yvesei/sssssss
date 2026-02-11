import jenkins.model.Jenkins
import org.jenkinsci.plugins.urlrewrite.UrlRewriteRulesFilterConfig
import org.eclipse.jetty.server.handler.RequestLogHandler
import org.eclipse.jetty.server.RequestLog
import org.eclipse.jetty.util.RolloverFileOutputStream

// Enable request logging in Jetty
try {
    def handler = Jenkins.getInstance().servletContext.getServer().getHandler()
    if (handler && !(handler instanceof RequestLogHandler)) {
        def requestLog = new org.eclipse.jetty.server.NCSARequestLog("/var/log/jenkins/access.log")
        requestLog.setRetainDays(1)
        requestLog.setAppend(true)
        requestLog.setExtended(false)
        
        def logHandler = new RequestLogHandler()
        logHandler.setRequestLog(requestLog)
        logHandler.setHandler(handler)
        
        Jenkins.getInstance().servletContext.getServer().setHandler(logHandler)
        requestLog.start()
        
        println("RequestLogHandler installed successfully")
    }
} catch (Exception e) {
    println("Error setting up request logging: " + e.message)
}

println("Jenkins initialization complete")
