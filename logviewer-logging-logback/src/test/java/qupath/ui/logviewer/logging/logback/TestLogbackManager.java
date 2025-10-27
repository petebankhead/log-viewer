package qupath.ui.logviewer.logging.logback;

import org.junit.jupiter.api.Assertions;
import qupath.ui.logviewer.api.LogMessage;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import qupath.ui.logviewer.api.listener.LoggerListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestLogbackManager {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(TestLogbackManager.class);
    private static final ch.qos.logback.classic.Logger logbackLogger = LogbackManager.getRootLogger();

    @Test
    void Check_Framework_Active() {
        LogbackManager logbackManager = new LogbackManager();

        boolean managerActive = logbackManager.isFrameworkActive();

        Assertions.assertTrue(managerActive);
    }

    @Test
    void Check_Root_Level_Set_To_Trace_Through_LogbackManager() {
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.setRootLogLevel(Level.TRACE);

        Level level = logbackManager.getRootLogLevel();

        Assertions.assertEquals(Level.TRACE, level);
    }
    @Test
    void Check_Root_Level_Set_To_Error_Through_Logback_Logger() {
        assert logbackLogger != null;
        LogbackManager logbackManager = new LogbackManager();
        logbackLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

        Level level = logbackManager.getRootLogLevel();

        Assertions.assertEquals(Level.ERROR, level);
    }

    @Test
    void Check_Message_Forwarded() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(_ -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        slf4jLogger.info("A log message 1");

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Not_Forwarded() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        LoggerListener loggerListener = _ -> latch.countDown();
        logbackManager.addListener(loggerListener);
        logbackManager.setRootLogLevel(Level.TRACE);
        logbackManager.removeListener(loggerListener);

        slf4jLogger.info("A log message 2");

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(_ -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        for (int i=0; i<N; ++i) {
            slf4jLogger.info("A log message 3");
        }

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Forwarded_From_Different_Threads() throws InterruptedException {
        int N = 5;
        CountDownLatch latch = new CountDownLatch(N);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(_ -> latch.countDown());
        logbackManager.setRootLogLevel(Level.TRACE);

        IntStream.range(0, N)
                .parallel()
                .forEach(_ -> slf4jLogger.info("A log message 4"));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Not_Forwarded_If_Level_Too_Low() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        logbackManager.addListener(_ -> latch.countDown());
        logbackManager.setRootLogLevel(Level.ERROR);

        slf4jLogger.info("A log message 5");

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Message_Information_Correct() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogbackManager logbackManager = new LogbackManager();
        LogMessage expectedLogMessage = new LogMessage(
                "qupath.ui.logviewer.logging.logback.TestLogbackManager",
                System.currentTimeMillis(),
                Thread.currentThread().getName(),
                Level.ERROR,
                "A description",
                new Throwable()
        );
        logbackManager.addListener(logMessage -> {
            // Test everything except the timestamp as it cannot be precisely predicted
            if (
                    logMessage.loggerName().equals(expectedLogMessage.loggerName()) &&
                    logMessage.threadName().equals(expectedLogMessage.threadName()) &&
                    logMessage.level().equals(expectedLogMessage.level()) &&
                    logMessage.message().equals(expectedLogMessage.message()) &&
                    logMessage.throwable().equals(expectedLogMessage.throwable())
            ) {
                latch.countDown();
            }
        });
        logbackManager.setRootLogLevel(Level.ERROR);

        slf4jLogger
                .atLevel(expectedLogMessage.level())
                .setMessage(expectedLogMessage.message())
                .setCause(expectedLogMessage.throwable())
                .log();

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
