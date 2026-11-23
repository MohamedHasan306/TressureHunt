package treasurehunt.search;

import treasurehunt.model.Position;
import treasurehunt.model.State;

import java.util.List;

public final class SearchResult {
    private final boolean found;
    private final State goalState;
    private final List<State> path;
    private final String solutionMoves;
    private final int pathLength;
    private final int totalCost;
    private final int expandedNodes;
    private final int discoveredNodes;
    private final long executionTimeNanos;
    private final String algorithmName;
    private final List<Position> exploredPositions;

    SearchResult(String algorithmName, State goalState, List<Position> exploredPositions,
                 int discoveredNodes, long startTimeNanos) {
        this.algorithmName = algorithmName;
        this.goalState = goalState;
        found = goalState != null;
        path = found ? goalState.reconstructPath() : List.of();
        solutionMoves = found ? goalState.getMovesString() : "";
        pathLength = solutionMoves.length();
        totalCost = found ? goalState.getGCost() : 0;
        this.exploredPositions = List.copyOf(exploredPositions);
        expandedNodes = exploredPositions.size();
        this.discoveredNodes = discoveredNodes;
        executionTimeNanos = System.nanoTime() - startTimeNanos;
    }

    public boolean isFound() {
        return found;
    }

    public State getGoalState() {
        return goalState;
    }

    public List<State> getPath() {
        return path;
    }

    public String getSolutionMoves() {
        return solutionMoves;
    }

    public int getPathLength() {
        return pathLength;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public int getExpandedNodes() {
        return expandedNodes;
    }

    public int getDiscoveredNodes() {
        return discoveredNodes;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Position> getExploredPositions() {
        return exploredPositions;
    }
}
