import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class Board extends JPanel implements ActionListener {

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    private static final int INITIAL_DELAY = 600;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isAutoplay = false;

    private int score = 0;
    private int linesCleared = 0;

    private Tetromino currentPiece;
    private int currentX = 0;
    private int currentY = 0;

    private Tetromino nextPiece;

    private final Color[][] board;
    private final AutoplayAI ai;

    private JLabel scoreLabel;
    private JLabel linesLabel;
    private JPanel nextPiecePanel;

    public Board() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE));
        setLayout(new BorderLayout());

        ai = new AutoplayAI(this);
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        timer = new Timer(INITIAL_DELAY, this);

        addKeyListener(new TAdapter());
    }

    public void setUiElements(JLabel scoreLabel, JLabel linesLabel, JPanel nextPiecePanel) {
        this.scoreLabel = scoreLabel;
        this.linesLabel = linesLabel;
        this.nextPiecePanel = nextPiecePanel;
    }

    public void start() {
        if (isPaused) return;
        isStarted = true;
        isFallingFinished = false;
        score = 0;
        linesCleared = 0;
        clearBoard();
        newPiece();
        timer.start();
        updateLabels();
    }

    public void pause() {
        if (!isStarted || isFallingFinished) return;
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    public void toggleAutoplay() {
        isAutoplay = !isAutoplay;
        if (isAutoplay) {
            isPaused = false;
            timer.stop();
            timer = new Timer(50, this);
            timer.start();
            System.out.println("Autoplay ON");
        } else {
            timer.stop();
            timer = new Timer(INITIAL_DELAY, this);
            if (isStarted && !isPaused) timer.start();
            System.out.println("Autoplay OFF");
        }
    }

    private void updateLabels() {
        if (scoreLabel != null) scoreLabel.setText("Score: " + score);
        if (linesLabel != null) linesLabel.setText("Lines: " + linesCleared);
        if (nextPiecePanel != null) {
            nextPiecePanel.repaint();
        }
    }

    private void newPiece() {
        if (nextPiece == null) {
            currentPiece = Tetromino.getRandomPiece();
        } else {
            currentPiece = nextPiece;
        }
        nextPiece = Tetromino.getRandomPiece();
        nextPiece.setRotation(0);

        currentPiece.setRotation(0);
        currentX = BOARD_WIDTH / 2 - currentPiece.getCurrentShape()[0].length / 2;
        currentY = 0;

        if (!canMove(currentPiece, currentX, currentY, currentPiece.getRotation())) {
            gameOver();
        }
        updateLabels();
    }

    private void gameOver() {
        currentPiece = null;
        timer.stop();
        isStarted = false;
        isFallingFinished = true;
        JLabel gameOverLabel = new JLabel("Game Over");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 40));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        removeAll();
        add(gameOverLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
        System.out.println("Game Over! Score: " + score);
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            Arrays.fill(board[i], null);
        }
    }

    private void pieceDropped() {
        placePieceOnBoard();
        removeFullLines();
        if (!isFallingFinished) {
            newPiece();
        }
        repaint();
    }

    private void placePieceOnBoard() {
        int[][] shape = currentPiece.getCurrentShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    board[currentY + i][currentX + j] = currentPiece.color;
                }
            }
        }
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == null) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, BOARD_WIDTH);
                }
                Arrays.fill(board[0], null);
                i++;
            }
        }

        if (numFullLines > 0) {
            linesCleared += numFullLines;
            if (numFullLines == 1) score += 100;
            else if (numFullLines == 2) score += 300;
            else if (numFullLines == 3) score += 500;
            else if (numFullLines == 4) score += 800;
            updateLabels();
        }
    }

    private void oneLineDown() {
        if (!canMove(currentPiece, currentX, currentY + 1, currentPiece.getRotation())) {
            pieceDropped();
        } else {
            currentY++;
        }
        repaint();
    }

    public void hardDrop() {
        if (!isStarted || isFallingFinished || currentPiece == null) return;
        int newY = currentY;
        while (canMove(currentPiece, currentX, newY + 1, currentPiece.getRotation())) {
            newY++;
        }
        currentY = newY;
        pieceDropped();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished || isPaused) {
            return;
        }

        if (isAutoplay) {
            if (currentPiece != null) {
                AutoplayAI.Move bestMove = ai.findBestMove(currentPiece, board);
                if (bestMove != null) {
                    currentPiece.setRotation(bestMove.rotation);
                    currentX = bestMove.x;
                    currentY = bestMove.y;
                    pieceDropped();
                } else {
                    gameOver();
                }
            } else {
                newPiece();
            }
        } else {
            oneLineDown();
        }
        repaint();
    }


    private void tryMove(Tetromino piece, int newX, int newY, int newRotation) {
        if (canMove(piece, newX, newY, newRotation)) {
            currentPiece = piece;
            currentPiece.setRotation(newRotation);
            currentX = newX;
            currentY = newY;
            repaint();
        }
    }

    public boolean canMove(Tetromino piece, int newX, int newY, int targetRotation) {
        int[][] shape = piece.getShapeForRotation(targetRotation);

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = newX + j;
                    int y = newY + i;

                    if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                        return false;
                    }
                    if (board[y][x] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        if (currentPiece != null && !isFallingFinished) {
            drawPiece(g, currentPiece, currentX, currentY, currentPiece.getCurrentShape());
        }
        if (isPaused && isStarted) {
            drawPauseScreen(g);
        }
    }

    private void drawBoard(Graphics g) {
        g.setColor(new Color(50, 50, 50));
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(0, i * BLOCK_SIZE, BOARD_WIDTH * BLOCK_SIZE, i * BLOCK_SIZE);
        }
        for (int i = 0; i <= BOARD_WIDTH; i++) {
            g.drawLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        }

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != null) {
                    drawSquare(g, j * BLOCK_SIZE, i * BLOCK_SIZE, board[i][j]);
                }
            }
        }
    }

    private void drawPiece(Graphics g, Tetromino piece, int x, int y, int[][] shape) {
        g.setColor(piece.color);
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    drawSquare(g, (x + j) * BLOCK_SIZE, (y + i) * BLOCK_SIZE, piece.color);
                }
            }
        }
    }

    public void drawNextPiece(Graphics g, JPanel panel) {
        if (nextPiece == null) return;
        g.setColor(panel.getBackground());
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());

        int[][] shape = nextPiece.getCurrentShape();
        Color color = nextPiece.color;

        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();

        int pieceWidth = shape[0].length * BLOCK_SIZE;
        int pieceHeight = shape.length * BLOCK_SIZE;

        int xOffset = (panelWidth - pieceWidth) / 2;
        int yOffset = (panelHeight - pieceHeight) / 2;


        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    drawSquare(g, xOffset + j * BLOCK_SIZE, yOffset + i * BLOCK_SIZE, color);
                }
            }
        }
    }

    private void drawSquare(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x + 1, y + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + BLOCK_SIZE - 1, x, y);
        g.drawLine(x, y, x + BLOCK_SIZE - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
        g.drawLine(x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + 1);
    }

    private void drawPauseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        String msg = "Paused";
        FontMetrics fm = g.getFontMetrics();
        int msgWidth = fm.stringWidth(msg);
        g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
    }

    public static int getBoardWidth() {
        return BOARD_WIDTH;
    }

    public static int getBoardHeight() {
        return BOARD_HEIGHT;
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || currentPiece == null || isPaused || isAutoplay) {
                return;
            }

            int keycode = e.getKeyCode();

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(currentPiece, currentX - 1, currentY, currentPiece.getRotation());
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(currentPiece, currentX + 1, currentY, currentPiece.getRotation());
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                    tryMove(currentPiece, currentX, currentY, (currentPiece.getRotation() + 1) % 4);
                    break;
                case KeyEvent.VK_SPACE:
                    hardDrop();
                    break;
            }
        }
    }
}