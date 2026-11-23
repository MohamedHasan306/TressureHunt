package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class BFS implements SearchAlgorithm {
    @Override
    public String name() {
        return "BFS";
    }

    @Override
    public SearchResult solve(State initialState) {
        long started = System.nanoTime();
        Queue<State> frontier = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        List<Position> expanded = new ArrayList<>();
        frontier.add(initialState);
        visited.add(initialState);
        while (!frontier.isEmpty()) {
            SearchAlgorithm.checkForCancellation();
            State current = frontier.remove();
            if (current.isWin()) {
                return new SearchResult(name(), current, expanded, visited.size(), started);
            }
            expanded.add(current.getPosition());
            for (State next : current.getPossibleNextStates()) {
                if (visited.add(next)) {
                    frontier.add(next);
                }
            }
        }
        return new SearchResult(name(), null, expanded, visited.size(), started);
    }
}
