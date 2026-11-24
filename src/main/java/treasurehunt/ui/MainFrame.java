package treasurehunt.ui;

import treasurehunt.game.SolutionPlayer;
import treasurehunt.game.SolutionPlayer.Status;
import treasurehunt.loader.LevelLoader;
import treasurehunt.model.Direction;
import treasurehunt.model.Level;
import treasurehunt.model.State;
import treasurehunt.search.SearchAlgorithm;
import treasurehunt.search.SearchResult;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public final class MainFrame extends JFrame {
    private static final String READY_TO_PLAY = "Use the arrow keys or WASD to reach the treasure.";

    private final List<SearchAlgorithm> algorithms;
    private final SolutionPlayer player;
    private final GridPanel board = new GridPanel();
    private final JComboBox<Level> levelSelect = new JComboBox<>();
    private final JComboBox<String> algorithmSelect;
    private final JButton solve = new JButton("Solve");
    private final JButton play = new JButton("Play Solution");
    private final JButton pause = new JButton("Pause");
    private final JButton step = new JButton("Step Forward");
    private final JButton copy = new JButton("Copy Solution");
    private final JTextField manual = new JTextField(22);
    private final JSlider speed = new JSlider(1, 20, 5);
    private final JLabel status = new JLabel();
    private final JLabel progress = new JLabel();
    private final JLabel algorithmName = new JLabel();
    private final JLabel pathLength = new JLabel();
    private final JLabel totalCost = new JLabel();
    private final JLabel expanded = new JLabel();
    private final JLabel discovered = new JLabel();
    private final JLabel executionTime = new JLabel();
    private final JTextArea solution = new JTextArea(4, 20);
    private SearchResult result;
    private SwingWorker<SearchResult, Void> searchWorker;
    private Timer animationTimer;

    public MainFrame(List<Level> levels, List<SearchAlgorithm> algorithms) {
        super("Treasure Hunt");
        this.algorithms = List.copyOf(algorithms);
        player = new SolutionPlayer(levels.get(0));
        levelSelect.setModel(new DefaultComboBoxModel<>(levels.toArray(Level[]::new)));
        algorithmSelect = new JComboBox<>(algorithms.stream().map(SearchAlgorithm::name).toArray(String[]::new));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        bindActions();
        showLevel(levels.get(0));
        pack();
        setMinimumSize(new Dimension(780, 550));
        setLocationByPlatform(true);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JButton load = new JButton("Load TXT");
        load.addActionListener(event -> loadLevels());
        JPanel selectors = row(labelFor("Level", levelSelect), levelSelect,
                labelFor("Algorithm", algorithmSelect), algorithmSelect, solve, load);
        content.add(selectors, BorderLayout.NORTH);

        JPanel map = new JPanel(new BorderLayout(0, 4));
        map.add(new JScrollPane(board), BorderLayout.CENTER);
        map.add(new JLabel("Blue: player   •   Gold: treasure   •   Green: path   •   Light blue: explored"),
                BorderLayout.SOUTH);
        content.add(map, BorderLayout.CENTER);
        content.add(buildResults(), BorderLayout.EAST);

        JButton reset = new JButton("Reset");
        reset.addActionListener(event -> {
            stopAnimation();
            player.reset();
            refreshPlayback();
            board.requestFocusInWindow();
        });
        JButton run = new JButton("Run Moves");
        run.addActionListener(event -> startPlayback(manual.getText()));
        manual.setToolTipText("Enter directions using U, D, L, and R.");
        speed.setMajorTickSpacing(5);
        speed.setPaintTicks(true);
        speed.setToolTipText("Playback speed in moves per second");
        speed.setPreferredSize(new Dimension(112, 32));
        JPanel playback = row(play, pause, step, reset, labelFor("Speed", speed), speed);
        JPanel manualControls = row(labelFor("Manual moves", manual), manual, run);
        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 2));
        bottom.add(row(new JLabel("Move (arrows / WASD)"),
                directionButton("↑", "up", Direction.UP),
                directionButton("←", "left", Direction.LEFT),
                directionButton("↓", "down", Direction.DOWN),
                directionButton("→", "right", Direction.RIGHT)));
        bottom.add(playback);
        bottom.add(manualControls);
        bottom.add(row(progress, status));
        content.add(bottom, BorderLayout.SOUTH);
        return content;
    }

    private JPanel buildResults() {
        JPanel results = new JPanel(new BorderLayout(0, 10));
        results.setBorder(BorderFactory.createTitledBorder("Search results"));
        JPanel metrics = new JPanel(new GridLayout(0, 2, 10, 8));
        addMetric(metrics, "Algorithm", algorithmName);
        addMetric(metrics, "Path length", pathLength);
        addMetric(metrics, "Total cost", totalCost);
        addMetric(metrics, "Expanded nodes", expanded);
        addMetric(metrics, "Discovered nodes", discovered);
        addMetric(metrics, "Execution time", executionTime);
        results.add(metrics, BorderLayout.NORTH);

        solution.setEditable(false);
        solution.setLineWrap(true);
        solution.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JPanel moves = new JPanel(new BorderLayout(0, 5));
        moves.add(labelFor("Solution (UDLR)", solution), BorderLayout.NORTH);
        moves.add(new JScrollPane(solution), BorderLayout.CENTER);
        moves.add(copy, BorderLayout.SOUTH);
        results.add(moves, BorderLayout.CENTER);
        return results;
    }

    private void bindActions() {
        board.setFocusable(true);
        board.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                board.requestFocusInWindow();
            }
        });
        bindDirection(Direction.UP, KeyEvent.VK_UP, KeyEvent.VK_W);
        bindDirection(Direction.LEFT, KeyEvent.VK_LEFT, KeyEvent.VK_A);
        bindDirection(Direction.DOWN, KeyEvent.VK_DOWN, KeyEvent.VK_S);
        bindDirection(Direction.RIGHT, KeyEvent.VK_RIGHT, KeyEvent.VK_D);

        levelSelect.addActionListener(event -> {
            Level selected = (Level) levelSelect.getSelectedItem();
            if (selected != null && selected != player.getLevel()) {
                showLevel(selected);
            }
        });
        solve.addActionListener(event -> solve());
        play.addActionListener(event -> startPlayback(result.getSolutionMoves()));
        pause.addActionListener(event -> {
            if (player.getStatus() == Status.PLAYING) {
                stopAnimation();
                player.pause();
            } else {
                player.resume();
                startAnimation();
            }
            refreshPlayback();
        });
        step.addActionListener(event -> {
            stopAnimation();
            if (player.getStatus() != Status.PLAYING && player.getStatus() != Status.PAUSED) {
                player.play(result.getSolutionMoves());
            }
            player.stepForward();
            refreshPlayback();
        });
        speed.addChangeListener(event -> {
            if (animationTimer != null) {
                animationTimer.setDelay(frameDelay());
            }
        });
        manual.addActionListener(event -> startPlayback(manual.getText()));
        copy.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(result.getSolutionMoves()), null);
                status.setText("Solution copied.");
            } catch (IllegalStateException e) {
                status.setText("Clipboard is busy. Try again.");
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                board.requestFocusInWindow();
            }

            @Override
            public void windowClosed(WindowEvent event) {
                stopAnimation();
                cancelSearch();
            }
        });
    }

    private void bindDirection(Direction direction, int arrowKey, int letterKey) {
        AbstractAction move = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!(getFocusOwner() instanceof JTextComponent)
                        && !(getFocusOwner() instanceof JComboBox<?>)) {
                    moveManually(direction);
                }
            }
        };
        for (int key : new int[]{arrowKey, letterKey}) {
            KeyStroke stroke = KeyStroke.getKeyStroke(key, 0);
            // The board's own binding takes precedence over its scroll pane.
            board.getInputMap(JComponent.WHEN_FOCUSED).put(stroke, direction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, direction);
        }
        board.getActionMap().put(direction, move);
        getRootPane().getActionMap().put(direction, move);
    }

    private JButton directionButton(String symbol, String label, Direction direction) {
        JButton button = new JButton(symbol);
        button.getAccessibleContext().setAccessibleName("Move " + label);
        button.addActionListener(event -> {
            moveManually(direction);
            board.requestFocusInWindow();
        });
        return button;
    }

    private void moveManually(Direction direction) {
        stopAnimation();
        player.moveManually(direction);
        refreshPlayback();
    }

    private void showLevel(Level level) {
        cancelSearch();
        stopAnimation();
        player.changeLevel(level);
        manual.setText("");
        board.setLevel(level.getGameGrid(), player.getPlayerPosition());
        showResult(null);
        refreshPlayback();
    }

    private void loadLevels() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Level text files (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            List<Level> levels = LevelLoader.load(chooser.getSelectedFile().toPath());
            levelSelect.setModel(new DefaultComboBoxModel<>(levels.toArray(Level[]::new)));
            showLevel(levels.get(0));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Could not load levels", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void solve() {
        cancelSearch();
        stopAnimation();
        player.pause();
        showResult(null);
        refreshPlayback();
        SearchAlgorithm algorithm = algorithms.get(algorithmSelect.getSelectedIndex());
        State initial = player.getLevel().getInitialState();
        status.setText("Searching with " + algorithm.name() + "…");
        solve.setEnabled(false);
        searchWorker = new SwingWorker<>() {
            @Override
            protected SearchResult doInBackground() {
                return algorithm.solve(initial);
            }

            @Override
            protected void done() {
                if (searchWorker != this) {
                    return;
                }
                searchWorker = null;
                solve.setEnabled(true);
                try {
                    SearchResult completed = get();
                    showResult(completed);
                    status.setText(completed.isFound() ? "Solution found." : "No path to the treasure.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText("Search interrupted.");
                } catch (ExecutionException e) {
                    status.setText("Search failed. " + e.getCause().getMessage());
                }
            }
        };
        searchWorker.execute();
    }

    private void cancelSearch() {
        if (searchWorker != null) {
            searchWorker.cancel(true);
            searchWorker = null;
        }
        solve.setEnabled(true);
    }

    private void showResult(SearchResult result) {
        this.result = result;
        board.setResult(result);
        boolean found = hasSolution();
        algorithmName.setText(result == null ? "—" : result.getAlgorithmName());
        pathLength.setText(found ? Integer.toString(result.getPathLength()) : "—");
        totalCost.setText(found ? Integer.toString(result.getTotalCost()) : "—");
        expanded.setText(result == null ? "—" : Integer.toString(result.getExpandedNodes()));
        discovered.setText(result == null ? "—" : Integer.toString(result.getDiscoveredNodes()));
        executionTime.setText(result == null ? "—" : String.format(Locale.ROOT, "%.3f ms",
                result.getExecutionTimeNanos() / 1_000_000.0));
        solution.setText(found ? result.getSolutionMoves() : "");
        solution.setCaretPosition(0);
        play.setEnabled(found);
        copy.setEnabled(found);
        updatePlaybackControls();
    }

    private boolean hasSolution() {
        return result != null && result.isFound();
    }

    private void startPlayback(String moves) {
        stopAnimation();
        player.play(moves);
        startAnimation();
        refreshPlayback();
    }

    private void startAnimation() {
        if (player.getStatus() != Status.PLAYING) {
            return;
        }
        animationTimer = new Timer(frameDelay(), event -> {
            if (event.getSource() != animationTimer) {
                return;
            }
            player.tick();
            if (player.getStatus() != Status.PLAYING) {
                stopAnimation();
            }
            refreshPlayback();
        });
        animationTimer.start();
    }

    private int frameDelay() {
        return 1000 / speed.getValue();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    private void refreshPlayback() {
        board.setPlayer(player.getPlayerPosition());
        Status current = player.getStatus();
        progress.setText(current == Status.MANUAL
                ? "Moves: " + player.getStep()
                : "Step " + player.getStep() + " / " + player.getMoveCount());
        status.setText(current == Status.READY ? READY_TO_PLAY : player.getMessage());
        updatePlaybackControls();
    }

    private void updatePlaybackControls() {
        Status current = player.getStatus();
        boolean active = current == Status.PLAYING || current == Status.PAUSED;
        pause.setEnabled(active);
        pause.setText(current == Status.PAUSED ? "Resume" : "Pause");
        step.setEnabled(active || hasSolution());
    }

    private static JPanel row(java.awt.Component... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 2));
        for (java.awt.Component component : components) {
            row.add(component);
        }
        return row;
    }

    private static JLabel labelFor(String text, javax.swing.JComponent field) {
        JLabel label = new JLabel(text);
        label.setLabelFor(field);
        return label;
    }

    private static void addMetric(JPanel panel, String title, JLabel value) {
        panel.add(new JLabel(title));
        panel.add(value);
    }
}
