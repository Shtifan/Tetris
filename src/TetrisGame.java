import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TetrisGame extends JPanel {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 30;

    private TetrisPiece currentPiece;
    private Color[][] board;
    private Timer timer;

    public TetrisGame() {
        setFocusable(true);
        initializeBoard();
        spawnPiece();
        initializeTimer();
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
                board[row][col] = Color.LIGHT_GRAY; // Light gray for empty cells
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
            if (!movePieceDown()) {
                placePieceOnBoard();
                clearFullRows();
                spawnPiece();
            }
            repaint();
        });
        timer.start();
    }

    private void handleInput(KeyEvent e) {
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
                if (canPlacePiece(rotatedPiece, rotatedPiece.getX(), rotatedPiece.getY())) {
                    currentPiece.rotate();
                }
                break;
            case KeyEvent.VK_SPACE:
                dropPieceToBottom();
                break;
        }
        repaint();
    }

    private boolean movePieceDown() {
        if (canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.setPosition(currentPiece.getX(), currentPiece.getY() + 1);
            return true;
        }
        return false;
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
        JOptionPane.showMessageDialog(this, "Game Over!", "Tetris", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPiece(g);
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                g.setColor(board[row][col]);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPiece(Graphics g) {
        if (currentPiece != null) {
            g.setColor(currentPiece.getColor());
            int[][] shape = currentPiece.getShape();
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[0].length; col++) {
                    if (shape[row][col] != 0) {
                        int x = currentPiece.getX() + col;
                        int y = currentPiece.getY() + row;
                        if (y >= 0) {
                            g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        TetrisGame game = new TetrisGame();
        frame.add(game);
        frame.setSize(BOARD_WIDTH * TILE_SIZE + 15, BOARD_HEIGHT * TILE_SIZE + 38); // Account for window decorations
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
}
