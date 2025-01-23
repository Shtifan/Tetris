package base;

import java.util.Random;
import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Color;

public class AutoPlay {
    private final GamePanel gamePanel;
    private final TetrisGame tetrisGame;
    private final Random random;
    private Timer autoplayTimer;
    private boolean isAutoplayActive;
    private final JButton autoplayButton;

    public AutoPlay(TetrisGame tetrisGame, GamePanel gamePanel, JButton autoplayButton) {
        this.tetrisGame = tetrisGame;
        this.gamePanel = gamePanel;
        this.autoplayButton = autoplayButton;
        this.random = new Random();
        this.isAutoplayActive = false;

        initializeAutoplayTimer();
    }

    private void initializeAutoplayTimer() {
        autoplayTimer = new Timer(300, e -> {
            if (tetrisGame.isPaused() || tetrisGame.isGameOver()) {
                stopAutoplay();
                return;
            }

            makeBestMove();

            if (random.nextInt(10) == 0) {
                dropPiece();
            }

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

    private void dropPiece() {
        boolean canMoveDown = true;
        while (canMoveDown) {
            canMoveDown = gamePanel.movePieceDown();
        }
        gamePanel.placePieceOnBoard();
        gamePanel.clearFullRows();
        gamePanel.spawnPiece();
    }

    private void makeBestMove() {
        int bestAction = evaluateBestMove();
        switch (bestAction) {
            case 0:
                gamePanel.movePiece(-1);
                break;
            case 1:
                gamePanel.movePiece(1);
                break;
            case 2:
                gamePanel.rotatePiece();
                break;
            case 3:
                break;
        }
    }

    private int evaluateBestMove() {
        int bestAction = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int action = 0; action < 4; action++) {
            simulateMove(action);
            double score = evaluateBoard();
            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
            revertMove(action);
        }

        return bestAction;
    }

    private void simulateMove(int action) {
        switch (action) {
            case 0:
                gamePanel.movePiece(-1);
                break;
            case 1:
                gamePanel.movePiece(1);
                break;
            case 2:
                gamePanel.rotatePiece();
                break;
            case 3:
                break;
        }
    }

    private void revertMove(int action) {
        switch (action) {
            case 0:
                gamePanel.movePiece(1);
                break;
            case 1:
                gamePanel.movePiece(-1);
                break;
            case 2:
                gamePanel.rotatePiece();
                gamePanel.rotatePiece();
                gamePanel.rotatePiece();
                break;
            case 3:
                break;
        }
    }

    private double evaluateBoard() {
        Color[][] board = gamePanel.getBoard();
        int rows = board.length;
        int cols = board[0].length;

        int holes = 0;
        int bumpiness = 0;
        int maxHeight = 0;
        int fullRows = 0;

        int[] columnHeights = new int[cols];
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                if (board[row][col] != Color.LIGHT_GRAY) {
                    columnHeights[col] = rows - row;
                    break;
                }
            }
        }

        for (int col = 0; col < cols; col++) {
            maxHeight = Math.max(maxHeight, columnHeights[col]);
        }

        for (int col = 0; col < cols - 1; col++) {
            bumpiness += Math.abs(columnHeights[col] - columnHeights[col + 1]);
        }

        for (int col = 0; col < cols; col++) {
            boolean blockFound = false;
            for (Color[] colors : board) {
                if (colors[col] != Color.LIGHT_GRAY) {
                    blockFound = true;
                } else if (blockFound) {
                    holes++;
                }
            }
        }

        for (Color[] colors : board) {
            boolean fullRow = true;
            for (int col = 0; col < cols; col++) {
                if (colors[col] == Color.LIGHT_GRAY) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                fullRows++;
            }
        }

        return -0.510066 * holes - 0.184483 * bumpiness - 0.35663 * maxHeight + 0.760666 * fullRows;
    }
}
