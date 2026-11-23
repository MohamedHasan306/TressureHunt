package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class Dijkstra implements SearchAlgorithm {
    private record Entry(State state, long insertionOrder) {
    }

    @Override
    public String name() {
        return "Dijkstra";
    }

    @Override
    public SearchResult solve(State initialState) {
        long started = System.nanoTime();
        Map<State, Integer> distances = new HashMap<>();
        List<Position> expanded = new ArrayList<>();
        PriorityQueue<Entry> frontier = new PriorityQueue<>(Comparator
                .comparingInt((Entry entry) -> entry.state().getGCost())
                .thenComparingLong(Entry::insertionOrder));
        long insertionOrder = 0;
        distances.put(initialState, initialState.getGCost());
        frontier.add(new Entry(initialState, insertionOrder++));
        while (!frontier.isEmpty()) {
            SearchAlgorithm.checkForCancellation();
            State current = frontier.remove().state();
            if (current.getGCost() != distances.get(current)) {
                continue;
            }
            if (current.isWin()) {
                return new SearchResult(name(), current, expanded, distances.size(), started);
            }
            expanded.add(current.getPosition());
            for (State next : current.getPossibleNextStates()) {
                int candidate = next.getGCost();
                if (candidate < distances.getOrDefault(next, Integer.MAX_VALUE)) {
                    distances.put(next, candidate);
                    // The fresh State already points to the parent that produced this cost.
                    frontier.add(new Entry(next, insertionOrder++));
                }
            }
        }
        return new SearchResult(name(), null, expanded, distances.size(), started);
    }
}
