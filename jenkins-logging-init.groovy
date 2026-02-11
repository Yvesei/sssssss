import java.util.logging.Logger
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter

println("Setting up Jenkins logging...")

try {
    // Create log directory
    def logDir = new File("/var/log/jenkins")
    logDir.mkdirs()
    
    // Configure root logger to output more details
    def logger = Logger.getLogger("hudson")
    logger.fine("Jenkins logging initialized")
    
    println("Jenkins logging configuration complete")
} catch (Exception e) {
    println("Error setting up logging: " + e.message)
    e.printStackTrace()
}
