package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DFS implements SearchAlgorithm {
    @Override
    public String name() {
        return "DFS (iterative)";
    }

    @Override
    public SearchResult solve(State initialState) {
        long started = System.nanoTime();
        Deque<State> frontier = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        Set<State> discovered = new HashSet<>();
        List<Position> expanded = new ArrayList<>();
        frontier.push(initialState);
        discovered.add(initialState);
        while (!frontier.isEmpty()) {
            SearchAlgorithm.checkForCancellation();
            State current = frontier.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (current.isWin()) {
                return new SearchResult(name(), current, expanded, discovered.size(), started);
            }
            expanded.add(current.getPosition());
            // Push D, R, U, L in lecture order. LIFO explores the last available direction first.
            for (State next : current.getPossibleNextStates()) {
                if (!visited.contains(next)) {
                    frontier.push(next);
                    discovered.add(next);
                }
            }
        }
        return new SearchResult(name(), null, expanded, discovered.size(), started);
    }
}
