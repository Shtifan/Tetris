import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import pieces.*;
import base.TetrisPiece;

public class GamePanel extends JPanel {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 40;

    private TetrisPiece currentPiece;
    private Color[][] board;
    private final Random random;
    private final TetrisGame tetrisGame;

    public GamePanel(TetrisGame tetrisGame) {
        this.tetrisGame = tetrisGame;
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
                if (!tetrisGame.isPaused() && !tetrisGame.isGameOver()) {
                    handleKeyPress(e.getKeyCode());
                }
            }
        });
    }

    private void handleKeyPress(int keyCode) {
        if (!tetrisGame.isPaused() && !tetrisGame.isGameOver()) {
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                    movePiece(-1);
                    break;
                case KeyEvent.VK_RIGHT:
                    movePiece(1);
                    break;
                case KeyEvent.VK_DOWN:
                    movePieceDown();
                    break;
                case KeyEvent.VK_UP:
                    rotatePiece();
                    break;
                case KeyEvent.VK_SPACE:
                    dropPieceToBottom();
                    break;
                case KeyEvent.VK_C:
                    holdPiece();
                    break;
            }
            repaint();
        }
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
        if (tetrisGame.getNextPieces().isEmpty()) {
            tetrisGame.getNextPieces().add(getRandomPiece());
        }

        currentPiece = tetrisGame.getNextPieces().remove(0);

        while (tetrisGame.getNextPieces().size() < 3) {
            tetrisGame.getNextPieces().add(getRandomPiece());
        }

        currentPiece.setPosition(BOARD_WIDTH / 2 - 1, 0);

        if (!canPlacePiece(currentPiece, currentPiece.getX(), currentPiece.getY())) {
            gameOver();
        }
    }

    private void gameOver() {
        tetrisGame.getTimer().stop();
        tetrisGame.setCanHoldPiece(false);
        JOptionPane.showMessageDialog(tetrisGame, "Game Over! Final Score: " + tetrisGame.getScore(), "Tetris", JOptionPane.INFORMATION_MESSAGE);
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
        if (!tetrisGame.isCanHoldPiece()) return;

        TetrisPiece temp = tetrisGame.getHeldPiece();
        tetrisGame.setHeldPiece(currentPiece);
        tetrisGame.setCanHoldPiece(false);

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
        tetrisGame.setCanHoldPiece(true);
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
            tetrisGame.setScore(tetrisGame.getScore() + multipliers[Math.min(rowsCleared, 4)]);
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
        if (tetrisGame.isPaused()) drawPauseOverlay(g);
        if (tetrisGame.isGameOver()) drawGameOverOverlay(g);
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + tetrisGame.getScore(), 10, getHeight() - 10);
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

        if (tetrisGame.getHeldPiece() != null) {
            drawPiecePreview(g, tetrisGame.getHeldPiece(), xOffset, yOffset + 20);
        }
    }

    private void drawNextPieces(Graphics g) {
        int xOffset = 10;
        int yOffset = 250;

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Next Pieces:", xOffset, yOffset + 80);

        int spacing = 100;
        for (int i = 0; i < tetrisGame.getNextPieces().size(); i++) {
            drawPiecePreview(g, tetrisGame.getNextPieces().get(i), xOffset, yOffset + spacing * (i + 1));
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
