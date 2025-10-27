package qupath.ui.logviewer.ui.main;

import org.junit.jupiter.api.Assertions;
import qupath.ui.logviewer.api.LogMessage;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestLogViewerModel {

    @BeforeAll
    static void initJfxRuntime() {
        JavaFXUtils.initJfxRuntime();
    }

    @Test
    void Check_Message_Added() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 1) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.TRACE, "", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_N_Message_Added() throws InterruptedException {
        int N = 10;
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == N) {
                latch.countDown();
            }
        });

        for (int i=0; i<N; ++i) {
            logViewerModel.addLogMessage(new LogMessage("", i, "", Level.TRACE, "", null));
        }

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Root_Log_Level_Set() {
        LogViewerModel logViewerModel = new LogViewerModel();
        Level expectedLevel = Level.ERROR;

        logViewerModel.setRootLevel(expectedLevel);
        Level level = logViewerModel.getRootLevel();

        Assertions.assertEquals(expectedLevel, level);
    }

    @Test
    void Check_Logging_Framework_Found() {
        LogViewerModel logViewerModel = new LogViewerModel();

        boolean loggingFrameworkFound = logViewerModel.getLoggingFrameworkFoundProperty().get();

        Assertions.assertTrue(loggingFrameworkFound);
    }

    @Test
    void Check_Single_Thread_Filter_Filters_Messages_With_Different_Thread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String threadName = "thread";
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getDisplayAllThreadsProperty().set(false);
        logViewerModel.displayOneThread(threadName);
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 5) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "", null));

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Single_Thread_Filter_Keeps_Messages_With_Same_Thread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String threadName = "thread";
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getDisplayAllThreadsProperty().set(false);
        logViewerModel.displayOneThread(threadName);
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 2) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_All_Thread_Filter_Keeps_All_Messages() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String threadName = "thread";
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.displayOneThread(threadName);
        logViewerModel.getDisplayAllThreadsProperty().set(true);
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 5) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, threadName, Level.TRACE, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Level_Filter_Filters_Messages_With_Different_Level() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.hideLogLevel(Level.ERROR.name());
        logViewerModel.hideLogLevel(Level.WARN.name());
        logViewerModel.hideLogLevel(Level.INFO.name());
        logViewerModel.hideLogLevel(Level.TRACE.name());
        logViewerModel.displayLogLevel(Level.DEBUG.name());
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 5) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.ERROR, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.WARN, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.INFO, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.DEBUG, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "", null));

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Level_Filter_Keeps_Messages_With_Different_Level() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.hideLogLevel(Level.ERROR.name());
        logViewerModel.hideLogLevel(Level.WARN.name());
        logViewerModel.hideLogLevel(Level.INFO.name());
        logViewerModel.hideLogLevel(Level.TRACE.name());
        logViewerModel.displayLogLevel(Level.DEBUG.name());
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 1) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.ERROR, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.WARN, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.INFO, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.DEBUG, "", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Regex_Filter_Filters_Messages_That_Do_Not_Match_Regex() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilterByRegexProperty().set(true);
        logViewerModel.getFilterProperty().set(".*(jim|joe).*");
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 5) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.TRACE, "jim", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "jom", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "joa", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.TRACE, "joe", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "jem", null));

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Regex_Filter_Keeps_Messages_That_Match_Regex() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilterByRegexProperty().set(true);
        logViewerModel.getFilterProperty().set(".*(jim|joe).*");
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 2) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.TRACE, "jim", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "jom", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "joa", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.TRACE, "joe", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "jem", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Text_Filter_Filters_Messages_That_Do_Not_Contain_Text() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilterByRegexProperty().set(false);
        logViewerModel.getFilterProperty().set("jim");
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 5) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.TRACE, "ljl jim", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "jom", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "joa", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.TRACE, "joe", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "jim qsdsqd", null));

        Assertions.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_Text_Filter_Keeps_Messages_That_Contain_Text() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getFilterByRegexProperty().set(false);
        logViewerModel.getFilterProperty().set("jim");
        logViewerModel.getFilteredLogs().addListener((ListChangeListener<? super LogMessage>) _ -> {
            if (logViewerModel.getFilteredLogs().size() == 2) {
                latch.countDown();
            }
        });

        logViewerModel.addLogMessage(new LogMessage("", 0, "", Level.TRACE, "ljl jim", null));
        logViewerModel.addLogMessage(new LogMessage("", 1, "", Level.TRACE, "jom", null));
        logViewerModel.addLogMessage(new LogMessage("", 2, "", Level.TRACE, "joa", null));
        logViewerModel.addLogMessage(new LogMessage("", 3, "", Level.TRACE, "joe", null));
        logViewerModel.addLogMessage(new LogMessage("", 4, "", Level.TRACE, "jim qsdsqd", null));

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void Check_All_Threads_Keeps_Messages_That_Contain_Text() throws InterruptedException {
        List<String> threads = Arrays.asList("a", "b", "c", "d", "e");
        CountDownLatch latch = new CountDownLatch(1);
        LogViewerModel logViewerModel = new LogViewerModel();
        logViewerModel.getAllThreads().addListener((SetChangeListener<? super String>) _ -> {
            if (logViewerModel.getAllThreads().containsAll(threads)) {
                latch.countDown();
            }
        });

        for (String thread : threads) {
            logViewerModel.addLogMessage(new LogMessage("", 0, thread, Level.TRACE, "", null));
        }

        Assertions.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
