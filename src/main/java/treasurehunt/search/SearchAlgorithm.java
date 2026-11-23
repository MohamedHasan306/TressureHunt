package treasurehunt.search;

import treasurehunt.model.State;

import java.util.concurrent.CancellationException;

public interface SearchAlgorithm {
    String name();

    SearchResult solve(State initialState);

    static void checkForCancellation() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Search cancelled.");
        }
    }
}
