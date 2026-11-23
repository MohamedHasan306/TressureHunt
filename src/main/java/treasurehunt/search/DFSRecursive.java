package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DFSRecursive implements SearchAlgorithm {
    @Override
    public String name() {
        return "DFS (recursive)";
    }

    @Override
    public SearchResult solve(State initialState) {
        long started = System.nanoTime();
        Set<State> visited = new HashSet<>();
        List<Position> expanded = new ArrayList<>();
        State goal;
        try {
            goal = visit(initialState, visited, expanded);
        } catch (StackOverflowError error) {
            throw new IllegalStateException("The map exceeded the recursive DFS stack. Use iterative DFS.", error);
        }
        return new SearchResult(name(), goal, expanded, visited.size(), started);
    }

    private State visit(State current, Set<State> visited, List<Position> expanded) {
        SearchAlgorithm.checkForCancellation();
        visited.add(current);
        if (current.isWin()) {
            return current;
        }
        expanded.add(current.getPosition());
        for (State next : current.getPossibleNextStates()) {
            if (!visited.contains(next)) {
                State goal = visit(next, visited, expanded);
                if (goal != null) {
                    return goal;
                }
            }
        }
        return null;
    }
}
