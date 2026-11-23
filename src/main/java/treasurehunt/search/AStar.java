package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class AStar implements SearchAlgorithm {
    private record Entry(State state, long fScore, long insertionOrder) {
    }

    @Override
    public String name() {
        return "A*";
    }

    @Override
    public SearchResult solve(State initialState) {
        long started = System.nanoTime();
        Position goal = initialState.getGrid().getTreasurePosition();
        Map<State, Integer> gScore = new HashMap<>();
        List<Position> expanded = new ArrayList<>();
        PriorityQueue<Entry> frontier = new PriorityQueue<>(Comparator
                .comparingLong(Entry::fScore)
                .thenComparingLong(Entry::insertionOrder));
        long insertionOrder = 0;
        gScore.put(initialState, initialState.getGCost());
        frontier.add(new Entry(initialState, fScore(initialState, goal), insertionOrder++));
        while (!frontier.isEmpty()) {
            SearchAlgorithm.checkForCancellation();
            State current = frontier.remove().state();
            if (current.getGCost() != gScore.get(current)) {
                continue;
            }
            if (current.isWin()) {
                return new SearchResult(name(), current, expanded, gScore.size(), started);
            }
            expanded.add(current.getPosition());
            for (State next : current.getPossibleNextStates()) {
                int candidate = next.getGCost();
                if (candidate < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
                    gScore.put(next, candidate);
                    frontier.add(new Entry(next, fScore(next, goal), insertionOrder++));
                }
            }
        }
        return new SearchResult(name(), null, expanded, gScore.size(), started);
    }

    private static long fScore(State state, Position goal) {
        long manhattan = Math.abs((long) state.getX() - goal.x()) + Math.abs((long) state.getY() - goal.y());
        return state.getGCost() + manhattan;
    }
}
