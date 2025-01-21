import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import pieces.*;
import base.TetrisPiece;

public class TetrisGame extends JFrame {
    private GamePanel gamePanel;
    private Timer timer;
    private int score;
    private boolean isPaused;
    private boolean isGameOver;
    private JButton pauseButton;
    private TetrisPiece heldPiece;
    private boolean canHoldPiece;
    private ArrayList<TetrisPiece> nextPieces;

    public TetrisGame() {
        super("Tetris");
        initializeGame();
        setupUI();
        initializeTimer();
    }

    private void initializeGame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        nextPieces = new ArrayList<>();
        score = 0;
        isPaused = false;
        isGameOver = false;
        canHoldPiece = true;
    }

    private void setupUI() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(Color.BLACK);

        getRootPane().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        gamePanel = new GamePanel(this);
        gamePanel.setLayout(null);

        JButton newGameButton = createButton("New Game", e -> resetGame());
        pauseButton = createButton("Pause", e -> togglePause());

        JButton exitButton = createButton("Exit", e -> System.exit(0));
        exitButton.setBounds(270, 10, 120, 40);

        newGameButton.setBounds(10, 10, 120, 40);
        pauseButton.setBounds(140, 10, 120, 40);

        gamePanel.add(newGameButton);
        gamePanel.add(pauseButton);
        gamePanel.add(exitButton);

        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(200, getHeight()));
        sidePanel.setOpaque(false);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        mainContainer.add(gamePanel, BorderLayout.CENTER);
        mainContainer.add(sidePanel, BorderLayout.EAST);

        setContentPane(mainContainer);
        setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusable(false);
        button.addActionListener(actionListener);
        return button;
    }

    private void togglePause() {
        if (isGameOver) return;

        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        gamePanel.repaint();
    }

    private void initializeTimer() {
        timer = new Timer(500, e -> {
            if (!gamePanel.movePieceDown()) {
                gamePanel.placePieceOnBoard();
                gamePanel.clearFullRows();
                gamePanel.spawnPiece();
                canHoldPiece = true;
            }
            gamePanel.repaint();
        });
        timer.start();
    }

    private void resetGame() {
        isPaused = false;
        isGameOver = false;
        score = 0;
        heldPiece = null;
        canHoldPiece = true;
        nextPieces.clear();

        for (int i = 0; i < 3; i++) {
            nextPieces.add(gamePanel.getRandomPiece());
        }

        gamePanel.initializeBoard();
        gamePanel.spawnPiece();
        timer.start();
        pauseButton.setText("Pause");
        gamePanel.requestFocusInWindow();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public ArrayList<TetrisPiece> getNextPieces() {
        return nextPieces;
    }

    public Timer getTimer() {
        return timer;
    }

    public TetrisPiece getHeldPiece() {
        return heldPiece;
    }

    public void setHeldPiece(TetrisPiece heldPiece) {
        this.heldPiece = heldPiece;
    }

    public boolean isCanHoldPiece() {
        return canHoldPiece;
    }

    public void setCanHoldPiece(boolean canHoldPiece) {
        this.canHoldPiece = canHoldPiece;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TetrisGame::new);
    }
}
