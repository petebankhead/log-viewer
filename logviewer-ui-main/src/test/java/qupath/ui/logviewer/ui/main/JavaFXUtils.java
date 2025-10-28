package qupath.ui.logviewer.ui.main;

import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities functions to help implementing unit tests on JavaFX objects.
 */
public final class JavaFXUtils {

    private static final Logger logger = LoggerFactory.getLogger(JavaFXUtils.class);
    private static JavaFxStatus javaFxStatus = JavaFxStatus.UNINITIALIZED;
    private enum JavaFxStatus {
        UNINITIALIZED,
        FAILED,
        INITIALIZED
    }

    /**
     * Initialize the JavaFX toolkit.
     */
    public synchronized static void initJfxRuntime() {
        if (javaFxStatus == JavaFxStatus.UNINITIALIZED) {
            try {
                Platform.startup(() -> {});
                javaFxStatus = JavaFxStatus.INITIALIZED;
            } catch (Exception e) {
                logger.error("Cannot initialize JavaFX Toolkit", e);
                javaFxStatus = JavaFxStatus.FAILED;
            }
        }

        if (javaFxStatus == JavaFxStatus.FAILED) {
            Assumptions.abort("Cannot initialize JavaFX Toolkit. Aborting tests");
        }
    }

    /**
     * Wait for the JavaFX thread to be run. To be used before assertions when working with observables.
     *
     * @throws InterruptedException when the program is interrupted
     */
    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }
}
