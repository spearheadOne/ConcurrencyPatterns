package org.abondar.experimental.concurrency.pattern;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

public class StructuredConcurrency {

    public List<Integer> runTasksInScope(List<Callable<Integer>> tasks) {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<Integer>allSuccessfulOrThrow())) {
            tasks.forEach(scope::fork);
            return scope.join()
                    .map(subtask -> subtask.get())
                    .toList();

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while running tasks", ex);
        } catch (StructuredTaskScope.FailedException ex) {
            throw new RuntimeException("Scope failure", ex);
        }
    }
}
