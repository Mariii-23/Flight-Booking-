import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jExample {
    private static final Logger logger = LogManager.getLogger(Log4jExample.class);

    public static void main(String[] args) throws InterruptedException {
        // Change log level in line 16 -> <Root level="info">

        logger.info("Starting the main!");

        logger.debug("Thread is going to sleep!");
        Thread.sleep(200);
        logger.debug("Thread is waking up!");

        logger.info("Stopping the main!");
    }
}
