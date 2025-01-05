import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

import pieces.*;
import base.TetrisPiece;

public class TetrisGame extends JFrame {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 40;

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

        gamePanel = new GamePanel();
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


    private class GamePanel extends JPanel {
        private TetrisPiece currentPiece;
        private Color[][] board;
        private final Random random;

        public GamePanel() {
            random = new Random();
            setFocusable(true);
            initializeBoard();
            spawnPiece();
            setupKeyListener();
        }

        private void setupKeyListener() {
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (!isPaused && !isGameOver) {
                        handleKeyPress(e.getKeyCode());
                    }
                }
            });
        }

        private void handleKeyPress(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_LEFT -> movePiece(-1);
                case KeyEvent.VK_RIGHT -> movePiece(1);
                case KeyEvent.VK_DOWN -> movePieceDown();
                case KeyEvent.VK_UP -> rotatePiece();
                case KeyEvent.VK_SPACE -> dropPieceToBottom();
                case KeyEvent.VK_C -> holdPiece();
            }
            repaint();
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
                nextPieces.add(getRandomPiece());
            }

            currentPiece = nextPieces.removeFirst();

            while (nextPieces.size() < 3) {
                nextPieces.add(getRandomPiece());
            }

            currentPiece.setPosition(BOARD_WIDTH / 2 - 1, 0);

            if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                gameOver();
            }
        }


        private void gameOver() {
            timer.stop();
            isGameOver = true;
            JOptionPane.showMessageDialog(TetrisGame.this,
                    "Game Over! Final Score: " + score,
                    "Tetris",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        public TetrisPiece getRandomPiece() {
            ArrayList<TetrisPiece> pieces = new ArrayList<>();
            pieces.add(new LPiece());
            pieces.add(new TPiece());
            pieces.add(new ZPiece());
            pieces.add(new SquarePiece());
            pieces.add(new IPiece());
            pieces.add(new ReverseLPiece());
            pieces.add(new ReverseZPiece());

            return pieces.get(random.nextInt(pieces.size()));
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

        private void movePiece(int dx) {
            if (canPlacePiece(currentPiece, currentPiece.getX() + dx, currentPiece.getY())) {
                currentPiece.setPosition(currentPiece.getX() + dx, currentPiece.getY());
            }
        }

        public boolean movePieceDown() {
            if (canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
                currentPiece.setPosition(currentPiece.getX(), currentPiece.getY() + 1);
                return true;
            }
            return false;
        }

        private void rotatePiece() {
            currentPiece.rotate();
            if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                if (!tryWallKick()) {
                    for (int i = 0; i < 3; i++) {
                        currentPiece.rotate();
                    }
                }
            }
        }

        private boolean tryWallKick() {
            int[] offsets = {1, -1, 2, -2};
            for (int offset : offsets) {
                if (canPlacePiece(currentPiece, currentPiece.getX() + offset, currentPiece.getY())) {
                    currentPiece.setPosition(currentPiece.getX() + offset, currentPiece.getY());
                    return true;
                }
            }
            return false;
        }

        private void dropPieceToBottom() {
            boolean canMoveDown = true;
            while (canMoveDown) {
                canMoveDown = movePieceDown();
            }
            placePieceOnBoard();
            clearFullRows();
            spawnPiece();
            canHoldPiece = true;
        }

        private boolean canPlacePiece(TetrisPiece piece, int x, int y) {
            int[][] shape = piece.getShape();
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
                        int newX = x + col;
                        int newY = y + row;

                        if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                            return false;
                        }

                        if (newY >= 0 && board[newY][newX] != Color.LIGHT_GRAY) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public void placePieceOnBoard() {
            int[][] shape = currentPiece.getShape();
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
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
            int rowsCleared = 0;
            for (int row = BOARD_HEIGHT - 1; row >= 0; row--) {
                if (isRowFull(row)) {
                    removeRow(row);
                    rowsCleared++;
                    row++;
                }
            }
            updateScore(rowsCleared);
        }

        private void updateScore(int rowsCleared) {
            int[] multipliers = {0, 100, 300, 500, 800};
            if (rowsCleared > 0) {
                score += multipliers[Math.min(rowsCleared, 4)];
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
            System.arraycopy(board, 0, board, 1, row);
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
            drawScore(g);
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
            g.drawString("Score: " + score, 10, getHeight() - 10);
        }

        private void drawBoard(Graphics g) {
            int xOffset = (getWidth() - BOARD_WIDTH * TILE_SIZE) / 2;
            int yOffset = (getHeight() - BOARD_HEIGHT * TILE_SIZE) / 2;

            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    drawTile(g, board[row][col], xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE);
                }
            }
        }

        private void drawTile(Graphics g, Color color, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
        }

        private void drawPiece(Graphics g) {
            if (currentPiece == null) return;

            int xOffset = (getWidth() - BOARD_WIDTH * TILE_SIZE) / 2;
            int yOffset = (getHeight() - BOARD_HEIGHT * TILE_SIZE) / 2;
            int[][] shape = currentPiece.getShape();

            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
                        int x = currentPiece.getX() + col;
                        int y = currentPiece.getY() + row;
                        if (y >= 0) {
                            drawTile(g, currentPiece.getColor(), xOffset + x * TILE_SIZE, yOffset + y * TILE_SIZE);
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
            g.drawString("Held Piece (C):", xOffset, yOffset);

            if (heldPiece != null) {
                drawPiecePreview(g, heldPiece, xOffset, yOffset + 20);
            }
        }

        private void drawNextPieces(Graphics g) {
            int xOffset = 10;
            int yOffset = 250;

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Next Pieces:", xOffset, yOffset + 80);

            int spacing = 100;
            for (int i = 0; i < nextPieces.size(); i++) {
                drawPiecePreview(g, nextPieces.get(i), xOffset, yOffset + spacing * (i + 1));
            }
        }

        private void drawPiecePreview(Graphics g, TetrisPiece piece, int xOffset, int yOffset) {
            int[][] shape = piece.getShape();
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
                        drawTile(g, piece.getColor(), xOffset + col * TILE_SIZE, yOffset + row * TILE_SIZE);
                    }
                }
            }
        }

        private void drawPauseOverlay(Graphics g) {
            drawOverlay(g, "PAUSED");
        }

        private void drawGameOverOverlay(Graphics g) {
            drawOverlay(g, "GAME OVER");
        }

        private void drawOverlay(Graphics g, String text) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(text)) / 2;
            int textY = getHeight() / 2;
            g.drawString(text, textX, textY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TetrisGame::new);
    }
}