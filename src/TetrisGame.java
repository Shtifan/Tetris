import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import pieces.*;
import base.TetrisPiece;

public class TetrisGame extends JFrame {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 40;

    private GamePanel gamePanel;
    private JPanel controlPanel;
    private Timer timer;
    private int score;
    private boolean isPaused = false;
    private JButton pauseButton;
    private TetrisPiece heldPiece = null;
    private boolean canHoldPiece = true;
    private Queue<TetrisPiece> nextPieces = new LinkedList<>();
    private boolean isGameOver = false;

    public TetrisGame() {
        super("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.BLACK);

        gamePanel = new GamePanel();

        // Create buttons and add them directly to the gamePanel
        JButton newGameButton = createButton("New Game", e -> resetGame());
        pauseButton = createButton("Pause", e -> togglePause());

        // Set the layout of the gamePanel to null for manual positioning
        gamePanel.setLayout(null);

        // Position the buttons manually
        newGameButton.setBounds(10, 10, 120, 40);
        pauseButton.setBounds(140, 10, 120, 40);

        // Add buttons to the gamePanel
        gamePanel.add(newGameButton);
        gamePanel.add(pauseButton);

        // Add gamePanel to main container
        mainContainer.add(gamePanel, BorderLayout.CENTER);

        // Create side panel for score, held piece, and next pieces with transparent background
        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(200, getHeight()));
        sidePanel.setOpaque(false);  // Make panel transparent
        mainContainer.add(sidePanel, BorderLayout.EAST);

        add(mainContainer);
        initializeTimer();

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
        if (isPaused) {
            timer.stop();
            pauseButton.setText("Resume");
        } else {
            timer.start();
            pauseButton.setText("Pause");
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
        gamePanel.initializeBoard();
        score = 0;
        heldPiece = null;
        canHoldPiece = true;
        nextPieces.clear();
        for (int i = 0; i < 3; i++) {
            nextPieces.add(gamePanel.getRandomPiece());
        }
        gamePanel.spawnPiece();
        timer.start();
        gamePanel.requestFocusInWindow();
    }

    private class GamePanel extends JPanel {
        private TetrisPiece currentPiece;
        private Color[][] board;

        public GamePanel() {
            setFocusable(true);
            initializeBoard();
            spawnPiece();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    handleInput(e);
                }
            });
        }

        public void initializeBoard() {
            board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[row][col] = Color.LIGHT_GRAY;
                }
            }
        }

        public void spawnPiece() {
            if (nextPieces.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    nextPieces.add(getRandomPiece());
                }
            }
            currentPiece = nextPieces.poll();
            nextPieces.add(getRandomPiece());
            currentPiece.setPosition(BOARD_WIDTH / 2 - 1, 0);
            if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                timer.stop();
                isGameOver = true;
                JOptionPane.showMessageDialog(TetrisGame.this, "Game Over", "Tetris", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        public TetrisPiece getRandomPiece() {
            List<TetrisPiece> pieces = List.of(
                    new LPiece(), new TPiece(), new ZPiece(), new SquarePiece(), new IPiece(), new ReverseLPiece(), new ReverseZPiece()
            );
            return pieces.get(new Random().nextInt(pieces.size()));
        }

        private void handleInput(KeyEvent e) {
            if (isPaused || isGameOver) return;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> movePiece(-1, 0);
                case KeyEvent.VK_RIGHT -> movePiece(1, 0);
                case KeyEvent.VK_DOWN -> movePieceDown();
                case KeyEvent.VK_UP -> rotatePiece();
                case KeyEvent.VK_SPACE -> dropPieceToBottom();
                case KeyEvent.VK_C -> holdPiece();
            }
            repaint();
        }

        private void holdPiece() {
            if (!canHoldPiece) return;

            TetrisPiece temp = heldPiece;
            heldPiece = currentPiece;
            canHoldPiece = false;

            if (temp == null) {
                spawnPiece();
            } else {
                currentPiece = temp;
                currentPiece.setPosition(BOARD_WIDTH / 2 - 1, 0);
            }
        }

        private void movePiece(int dx, int dy) {
            if (canPlacePiece(currentPiece, currentPiece.getX() + dx, currentPiece.getY() + dy)) {
                currentPiece.setPosition(currentPiece.getX() + dx, currentPiece.getY() + dy);
            }
        }

        private boolean movePieceDown() {
            if (canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
                currentPiece.setPosition(currentPiece.getX(), currentPiece.getY() + 1);
                return true;
            }
            return false;
        }

        private void rotatePiece() {
            currentPiece.rotate();
            if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                currentPiece.rotate();
            }
        }

        private void dropPieceToBottom() {
            while (movePieceDown()) ;
            placePieceOnBoard();
            clearFullRows();
            spawnPiece();
        }

        private boolean canPlacePiece(TetrisPiece piece, int x, int y) {
            for (int row = 0; row < piece.getShape().length; row++) {
                for (int col = 0; col < piece.getShape()[0].length; col++) {
                    if (piece.getShape()[row][col] != 0) {
                        int newX = x + col;
                        int newY = y + row;
                        if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT
                                || (newY >= 0 && board[newY][newX] != Color.LIGHT_GRAY)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public void placePieceOnBoard() {
            for (int row = 0; row < currentPiece.getShape().length; row++) {
                for (int col = 0; col < currentPiece.getShape()[0].length; col++) {
                    if (currentPiece.getShape()[row][col] != 0) {
                        int x = currentPiece.getX() + col;
                        int y = currentPiece.getY() + row;
                        if (y >= 0) {
                            board[y][x] = currentPiece.getColor();
                        }
                    }
                }
            }
        }

        public void clearFullRows() {
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                if (isRowFull(row)) {
                    removeRow(row);
                    score += 100;
                }
            }
        }

        private boolean isRowFull(int row) {
            for (Color cell : board[row]) {
                if (cell == Color.LIGHT_GRAY) {
                    return false;
                }
            }
            return true;
        }

        private void removeRow(int row) {
            for (int r = row; r > 0; r--) {
                board[r] = board[r - 1];
            }
            board[0] = new Color[BOARD_WIDTH];
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[0][col] = Color.LIGHT_GRAY;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBackground(g);
            drawBoard(g);
            drawPiece(g);
            drawHeldPiece(g);
            drawNextPieces(g);
            drawScore(g); // Moved this line to ensure the score is drawn last
            if (isPaused) drawPauseOverlay(g);
            if (isGameOver) drawGameOverOverlay(g);
        }

        private void drawBackground(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        private void drawScore(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));

            // Calculate the position for the score at the bottom left
            int x = 10; // Left margin
            int y = getHeight() - 10; // Bottom margin

            g.drawString("Score: " + score, x, y);
        }

        private void drawBoard(Graphics g) {
            int xOffset = (getWidth() - BOARD_WIDTH * TILE_SIZE) / 2;
            int yOffset = (getHeight() - BOARD_HEIGHT * TILE_SIZE) / 2;

            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    g.setColor(board[row][col]);
                    g.fillRect(xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        private void drawPiece(Graphics g) {
            int xOffset = (getWidth() - BOARD_WIDTH * TILE_SIZE) / 2;
            int yOffset = (getHeight() - BOARD_HEIGHT * TILE_SIZE) / 2;

            for (int row = 0; row < currentPiece.getShape().length; row++) {
                for (int col = 0; col < currentPiece.getShape()[0].length; col++) {
                    if (currentPiece.getShape()[row][col] != 0) {
                        int x = currentPiece.getX() + col;
                        int y = currentPiece.getY() + row;
                        if (y >= 0) {
                            g.setColor(currentPiece.getColor());
                            g.fillRect(xOffset + x * TILE_SIZE, yOffset + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            g.setColor(Color.DARK_GRAY);
                            g.drawRect(xOffset + x * TILE_SIZE, yOffset + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }

        private void drawHeldPiece(Graphics g) {
            int xOffset = 10;
            int yOffset = 100;

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Held Piece:", xOffset, yOffset);

            if (heldPiece != null) {
                for (int row = 0; row < heldPiece.getShape().length; row++) {
                    for (int col = 0; col < heldPiece.getShape()[0].length; col++) {
                        if (heldPiece.getShape()[row][col] != 0) {
                            g.setColor(heldPiece.getColor());
                            g.fillRect(xOffset + col * TILE_SIZE, yOffset + 20 + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            g.setColor(Color.DARK_GRAY);
                            g.drawRect(xOffset + col * TILE_SIZE, yOffset + 20 + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }

        private void drawNextPieces(Graphics g) {
            int xOffset = 10;
            int yOffset = 250;

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Next Pieces:", xOffset, yOffset + 80);

            int index = 0;
            for (TetrisPiece piece : nextPieces) {
                yOffset += 100;
                for (int row = 0; row < piece.getShape().length; row++) {
                    for (int col = 0; col < piece.getShape()[0].length; col++) {
                        if (piece.getShape()[row][col] != 0) {
                            g.setColor(piece.getColor());
                            g.fillRect(xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                            g.setColor(Color.DARK_GRAY);
                            g.drawRect(xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
                index++;
            }
        }

        private void drawPauseOverlay(Graphics g) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String pausedText = "PAUSED";
            FontMetrics fm = g.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(pausedText)) / 2;
            int textY = getHeight() / 2;
            g.drawString(pausedText, textX, textY);
        }

        private void drawGameOverOverlay(Graphics g) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String gameOverText = "GAME OVER";
            FontMetrics fm = g.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            int textY = getHeight() / 2;
            g.drawString(gameOverText, textX, textY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TetrisGame::new);
    }
}
