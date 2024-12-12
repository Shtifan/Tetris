import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pieces.*;
import base.TetrisPiece;

public class TetrisGame extends JPanel {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 40;

    private TetrisPiece currentPiece;
    private Color[][] board;
    private Timer timer;
    private int score;
    private boolean isPaused;

    public TetrisGame() {
        setFocusable(true);
        initializeBoard();
        spawnPiece();
        initializeTimer();
        score = 0;
        isPaused = false;

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    private void initializeBoard() {
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = Color.LIGHT_GRAY;
            }
        }
    }

    private void spawnPiece() {
        currentPiece = getRandomPiece();
        currentPiece.setPosition(BOARD_WIDTH / 2 - 1, 0);
        if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            gameOver();
        }
    }

    private TetrisPiece getRandomPiece() {
        List<TetrisPiece> pieces = new ArrayList<>();
        pieces.add(new LPiece());
        pieces.add(new TPiece());
        pieces.add(new ZPiece());
        pieces.add(new SquarePiece());
        pieces.add(new IPiece());
        pieces.add(new ReverseLPiece());
        pieces.add(new ReverseZPiece());
        return pieces.get(new Random().nextInt(pieces.size()));
    }

    private void initializeTimer() {
        timer = new Timer(500, e -> {
            if (!isPaused) {
                if (!movePieceDown()) {
                    placePieceOnBoard();
                    clearFullRows();
                    spawnPiece();
                }
                repaint();
            }
        });
        timer.start();
    }

    private void handleInput(KeyEvent e) {
        if (isPaused && e.getKeyCode() != KeyEvent.VK_P) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (canPlacePiece(currentPiece, currentPiece.getX() - 1, currentPiece.getY())) {
                    currentPiece.setPosition(currentPiece.getX() - 1, currentPiece.getY());
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (canPlacePiece(currentPiece, currentPiece.getX() + 1, currentPiece.getY())) {
                    currentPiece.setPosition(currentPiece.getX() + 1, currentPiece.getY());
                }
                break;
            case KeyEvent.VK_DOWN:
                movePieceDown();
                break;
            case KeyEvent.VK_UP:
                TetrisPiece rotatedPiece = getRotatedPiece();
                adjustPiecePositionToBounds(rotatedPiece);
                if (canPlacePiece(rotatedPiece, rotatedPiece.getX(), rotatedPiece.getY())) {
                    currentPiece.rotate();
                    adjustPiecePositionToBounds(currentPiece);
                }
                break;
            case KeyEvent.VK_SPACE:
                dropPieceToBottom();
                break;
            case KeyEvent.VK_P:
                togglePause();
                break;
        }
        repaint();
    }

    private void togglePause() {
        isPaused = !isPaused;
        repaint();
    }

    private boolean movePieceDown() {
        if (canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.setPosition(currentPiece.getX(), currentPiece.getY() + 1);
            return true;
        }
        return false;
    }

    private void adjustPiecePositionToBounds(TetrisPiece piece) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        int[][] shape = piece.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (shape[row][col] != 0) {
                    int x = piece.getX() + col;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                }
            }
        }

        if (minX < 0) {
            piece.setPosition(piece.getX() - minX, piece.getY());
        } else if (maxX >= BOARD_WIDTH) {
            piece.setPosition(piece.getX() - (maxX - BOARD_WIDTH + 1), piece.getY());
        }
    }

    private void dropPieceToBottom() {
        while (canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.setPosition(currentPiece.getX(), currentPiece.getY() + 1);
        }
        placePieceOnBoard();
        clearFullRows();
        spawnPiece();
    }

    private boolean canPlacePiece(TetrisPiece piece, int x, int y) {
        int[][] shape = piece.getShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (shape[row][col] != 0) {
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

    private TetrisPiece getRotatedPiece() {
        TetrisPiece rotatedPiece = null;
        try {
            rotatedPiece = currentPiece.getClass().getDeclaredConstructor().newInstance();
            rotatedPiece.setPosition(currentPiece.getX(), currentPiece.getY());
            rotatedPiece.rotate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotatedPiece;
    }

    private void placePieceOnBoard() {
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

    private void clearFullRows() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            boolean fullRow = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == Color.LIGHT_GRAY) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                score += 100;
                for (int r = row; r > 0; r--) {
                    board[r] = board[r - 1];
                }
                board[0] = new Color[BOARD_WIDTH];
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    board[0][col] = Color.LIGHT_GRAY;
                }
            }
        }
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over!\nScore: " + score, "Tetris", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawScore(g);
        drawBoard(g);
        drawPiece(g);
        if (isPaused) {
            drawPauseOverlay(g);
        }
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 10, 30);
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
        if (currentPiece != null) {
            int[][] shape = currentPiece.getShape();
            int xOffset = (getWidth() - BOARD_WIDTH * TILE_SIZE) / 2;
            int yOffset = (getHeight() - BOARD_HEIGHT * TILE_SIZE) / 2;

            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
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
    }

    private void drawPauseOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Paused", getWidth() / 2 - 100, getHeight() / 2);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        TetrisGame game = new TetrisGame();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Start maximized
        frame.setVisible(true);
    }
}
