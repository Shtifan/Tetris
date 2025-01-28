package base;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Color;

public class AutoPlay {
    private final GamePanel gamePanel;
    private final TetrisGame tetrisGame;
    private Timer autoplayTimer;
    private boolean isAutoplayActive;
    private final JButton autoplayButton;

    public AutoPlay(TetrisGame tetrisGame, GamePanel gamePanel, JButton autoplayButton) {
        this.tetrisGame = tetrisGame;
        this.gamePanel = gamePanel;
        this.autoplayButton = autoplayButton;
        this.isAutoplayActive = false;

        initializeAutoplayTimer();
    }

    private void initializeAutoplayTimer() {
        autoplayTimer = new Timer(100, e -> {
            if (tetrisGame.isPaused() || tetrisGame.isGameOver()) {
                stopAutoplay();
                return;
            }

            makeBestMove();
            gamePanel.repaint();
        });
    }

    public void toggleAutoplay() {
        if (tetrisGame.isGameOver()) {
            tetrisGame.resetGame();
        }

        if (isAutoplayActive) {
            stopAutoplay();
        } else {
            startAutoplay();
        }
    }

    private void startAutoplay() {
        if (!isAutoplayActive) {
            isAutoplayActive = true;
            autoplayTimer.start();
            autoplayButton.setText("Stop Auto");
        }
    }

    private void stopAutoplay() {
        if (isAutoplayActive) {
            isAutoplayActive = false;
            autoplayTimer.stop();
            autoplayButton.setText("Autoplay");
        }
    }

    private void makeBestMove() {
        Placement bestPlacement = findBestPlacement();
        executePlacement(bestPlacement);
        dropPiece();
    }

    private Placement findBestPlacement() {
        TetrisPiece currentPiece = gamePanel.getCurrentPiece();
        Color[][] currentBoard = gamePanel.getBoard();
        Placement best = new Placement(0, 0, 0, Double.NEGATIVE_INFINITY);

        for (int rotation = 0; rotation < 4; rotation++) {
            TetrisPiece rotated = currentPiece.getRotated(rotation);
            int maxX = GamePanel.BOARD_WIDTH - rotated.getWidth();
            for (int x = 0; x <= maxX; x++) {
                int y = findDropY(currentBoard, rotated, x);
                if (y < 0) continue;

                Color[][] simulated = simulatePlacement(currentBoard, rotated, x, y);
                double score = evaluateBoard(simulated, y + rotated.getHeight());
                if (score > best.score) {
                    best = new Placement(rotation, x, y, score);
                }
            }
        }

        return best;
    }

    private int findDropY(Color[][] board, TetrisPiece piece, int x) {
        int y = 0;
        while (canMoveDown(board, piece, x, y)) {
            y++;
        }
        return y - 1 >= 0 ? y - 1 : -1;
    }

    private boolean canMoveDown(Color[][] board, TetrisPiece piece, int x, int y) {
        if (y + piece.getHeight() >= GamePanel.BOARD_HEIGHT) return false;
        for (int i = 0; i < piece.getSize(); i++) {
            for (int j = 0; j < piece.getSize(); j++) {
                if (piece.getShape(i, j) != 0) {
                    int boardY = y + j + 1;
                    int boardX = x + i;
                    if (boardY >= GamePanel.BOARD_HEIGHT || board[boardY][boardX] != Color.LIGHT_GRAY) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Color[][] simulatePlacement(Color[][] original, TetrisPiece piece, int x, int y) {
        Color[][] copy = new Color[GamePanel.BOARD_HEIGHT][GamePanel.BOARD_WIDTH];
        for (int i = 0; i < copy.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, copy[i].length);
        }

        for (int i = 0; i < piece.getSize(); i++) {
            for (int j = 0; j < piece.getSize(); j++) {
                if (piece.getShape(i, j) != 0) {
                    copy[y + j][x + i] = piece.getColor();
                }
            }
        }
        return copy;
    }

    private void executePlacement(Placement placement) {
        TetrisPiece current = gamePanel.getCurrentPiece();
        int currentRotation = current.getRotation();
        int rotationsNeeded = (placement.rotation - currentRotation + 4) % 4;

        for (int i = 0; i < rotationsNeeded; i++) {
            gamePanel.rotatePiece();
        }

        int currentX = gamePanel.getCurrentPieceX();
        int deltaX = placement.x - currentX;
        int move = deltaX > 0 ? 1 : -1;
        for (int i = 0; i < Math.abs(deltaX); i++) {
            gamePanel.movePiece(move);
        }
    }

    private void dropPiece() {
        gamePanel.hardDropPiece();
    }

    private double evaluateBoard(Color[][] board, int landingHeight) {
        int holes = 0;
        int bumpiness = 0;
        int[] heights = new int[GamePanel.BOARD_WIDTH];
        int aggregateHeight = 0;
        int linesCleared = 0;

        for (int col = 0; col < GamePanel.BOARD_WIDTH; col++) {
            for (int row = 0; row < GamePanel.BOARD_HEIGHT; row++) {
                if (board[row][col] != Color.LIGHT_GRAY) {
                    heights[col] = GamePanel.BOARD_HEIGHT - row;
                    break;
                }
            }
            aggregateHeight += heights[col];
        }

        for (Color[] row : board) {
            boolean full = true;
            for (Color cell : row) {
                if (cell == Color.LIGHT_GRAY) {
                    full = false;
                    break;
                }
            }
            if (full) linesCleared++;
        }

        for (int col = 0; col < GamePanel.BOARD_WIDTH - 1; col++) {
            bumpiness += Math.abs(heights[col] - heights[col + 1]);
        }

        for (int col = 0; col < GamePanel.BOARD_WIDTH; col++) {
            boolean blockFound = false;
            for (int row = 0; row < GamePanel.BOARD_HEIGHT; row++) {
                if (board[row][col] != Color.LIGHT_GRAY) {
                    blockFound = true;
                } else if (blockFound) {
                    holes++;
                }
            }
        }

        double a = -0.510066;
        double b = -0.184483;
        double c = -0.35663;
        double d = 0.760666;
        double e = -0.3;

        return a * holes + b * bumpiness + c * aggregateHeight + d * linesCleared + e * landingHeight;
    }

    private static class Placement {
        int rotation;
        int x;
        int y;
        double score;

        Placement(int rotation, int x, int y, double score) {
            this.rotation = rotation;
            this.x = x;
            this.y = y;
            this.score = score;
        }
    }
}
