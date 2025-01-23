package base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TetrisGame extends JFrame {
    private GamePanel gamePanel;
    private Timer timer;
    private int score;
    private boolean isPaused;
    private boolean isGameOver;
    private JButton pauseButton;
    private TetrisPiece heldPiece;
    private boolean canHoldPiece;
    private TetrisPiece[] nextPieces;
    private int nextPiecesCount;

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
        nextPieces = new TetrisPiece[3];
        nextPiecesCount = 0;
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
        JButton autoplayButton = createButton("Autoplay", e -> {
        });
        JButton exitButton = createButton("Exit", e -> System.exit(0));

        newGameButton.setBounds(10, 10, 120, 40);
        pauseButton.setBounds(140, 10, 120, 40);
        exitButton.setBounds(270, 10, 120, 40);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        autoplayButton.setBounds((int) screenSize.getWidth() - 130, 10, 120, 40);

        gamePanel.add(newGameButton);
        gamePanel.add(pauseButton);
        gamePanel.add(autoplayButton);
        gamePanel.add(exitButton);

        mainContainer.add(gamePanel, BorderLayout.CENTER);

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
        nextPiecesCount = 0;

        for (int i = 0; i < 3; i++) {
            nextPieces[nextPiecesCount++] = gamePanel.getRandomPiece();
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

    public TetrisPiece[] getNextPieces() {
        return nextPieces;
    }

    public int getNextPiecesCount() {
        return nextPiecesCount;
    }

    public void setNextPiecesCount(int nextPiecesCount) {
        this.nextPiecesCount = nextPiecesCount;
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
