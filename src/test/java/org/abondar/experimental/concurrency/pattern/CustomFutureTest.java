package org.abondar.experimental.concurrency.pattern;

import org.abondar.experimental.concurrency.pattern.future.CustomFuture;
import org.abondar.experimental.concurrency.pattern.future.FutureException;
import org.abondar.experimental.concurrency.pattern.future.FutureState;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomFutureTest {

    @Test
    public void customFuturePending() {
        var future = new CustomFuture<String>();
        assertEquals(FutureState.PENDING, future.getState());
    }

    @Test
    public void customFutureComplete() throws Exception {
        var future = new CustomFuture<String>();
        var val = "VALUE";

        var res = future.complete(val);
        assertTrue(res);

        var isCompleted = future.isCompleted();
        assertTrue(isCompleted);

        var futureRes = future.get();
        assertEquals(val, futureRes);
    }

    @Test
    public void customFutureCancel() {
        var future = new CustomFuture<String>();
        assertTrue(future.cancel());

        var isCancelled = future.isCancelled();
        assertTrue(isCancelled);
        assertThrows(FutureException.class, future::get);
    }

    @Test
    public void failFuture() {
        var future = new CustomFuture<String>();
        var ex = new Exception("Test exception");
        assertTrue(future.fail(ex));

        assertThrows(FutureException.class, future::get);
    }

    @Test
    public void getWaitsUntilFutureCompletes() throws Exception {
        var future = new CustomFuture<String>();

        assertThrows(AssertionFailedError.class, () ->
                assertTimeoutPreemptively(Duration.ofMillis(100), future::get));
    }
}
