import java.awt.*;

public class AutoplayAI {
    private static final double WEIGHT_AGGREGATE_HEIGHT = -0.510066;
    private static final double WEIGHT_COMPLETED_LINES = 0.760666;
    private static final double WEIGHT_HOLES = -0.35663;
    private static final double WEIGHT_BUMPINESS = -0.184483;

    public AutoplayAI(Board boardRef) {

    }

    static class Move {
        int x, y;
        int rotation;
        double score;

        public Move(int x, int y, int rotation, double score) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.score = score;
        }
    }

    public Move findBestMove(Tetromino currentPiece, Color[][] currentBoardState) {
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int r = 0; r < 4; r++) {
            currentPiece.setRotation(r);
            int[][] pieceShape = currentPiece.getCurrentShape();

            for (int x = -pieceShape[0].length + 1; x < Board.getBoardWidth(); x++) {
                Color[][] tempBoard = copyBoard(currentBoardState);
                int landingY = simulateDrop(currentPiece, x, tempBoard);

                if (landingY == -1) continue;

                placePieceOnTempBoard(tempBoard, currentPiece, x, landingY);

                double score = evaluateBoard(tempBoard);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Move(x, landingY, r, score);
                }
            }
        }
        return bestMove;
    }

    private int simulateDrop(Tetromino piece, int startX, Color[][] boardState) {
        int currentY = 0;
        int[][] shape = piece.getCurrentShape();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = startX + c;
                    if (boardX < 0 || boardX >= Board.getBoardWidth()) return -1;
                    if (currentY + r < Board.getBoardHeight() && boardState[currentY + r][boardX] != null)
                        return -1;
                }
            }
        }

        while (canPlace(piece, startX, currentY + 1, boardState)) {
            currentY++;
        }
        return currentY;
    }

    private boolean canPlace(Tetromino piece, int x, int y, Color[][] boardState) {
        int[][] shape = piece.getCurrentShape();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = x + c;
                    int boardY = y + r;
                    if (boardX < 0 || boardX >= Board.getBoardWidth() || boardY < 0 || boardY >= Board.getBoardHeight()) {
                        return false;
                    }
                    if (boardState[boardY][boardX] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placePieceOnTempBoard(Color[][] board, Tetromino piece, int x, int y) {
        int[][] shape = piece.getCurrentShape();
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (y + r >= 0 && y + r < Board.getBoardHeight() && x + c >= 0 && x + c < Board.getBoardWidth()) {
                        board[y + r][x + c] = piece.color;
                    } else {
                    }
                }
            }
        }
    }

    private double evaluateBoard(Color[][] boardState) {
        int completedLines = countAndClearCompletedLines(boardState);
        int aggregateHeight = getAggregateHeight(boardState);
        int holes = getHoles(boardState);
        int bumpiness = getBumpiness(boardState);

        for (int c = 0; c < Board.getBoardWidth(); c++) {
            if (boardState[0][c] != null && aggregateHeight > Board.getBoardHeight() - 2) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        return WEIGHT_AGGREGATE_HEIGHT * aggregateHeight + WEIGHT_COMPLETED_LINES * completedLines + WEIGHT_HOLES * holes + WEIGHT_BUMPINESS * bumpiness;
    }

    private int getAggregateHeight(Color[][] board) {
        int totalHeight = 0;
        int[] heights = getColumnHeights(board);
        for (int height : heights) {
            totalHeight += height;
        }
        return totalHeight;
    }

    private int countAndClearCompletedLines(Color[][] board) {
        int lines = 0;
        for (int r = Board.getBoardHeight() - 1; r >= 0; r--) {
            boolean lineFull = true;
            for (int c = 0; c < Board.getBoardWidth(); c++) {
                if (board[r][c] == null) {
                    lineFull = false;
                    break;
                }
            }
            if (lineFull) {
                lines++;
                for (int k = r; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, Board.getBoardWidth());
                }
                for (int c = 0; c < Board.getBoardWidth(); c++) board[0][c] = null;
            }
        }
        return lines;
    }

    private int getHoles(Color[][] board) {
        int holes = 0;
        for (int c = 0; c < Board.getBoardWidth(); c++) {
            boolean blockFound = false;
            for (int r = 0; r < Board.getBoardHeight(); r++) {
                if (board[r][c] != null) {
                    blockFound = true;
                } else if (blockFound && board[r][c] == null) {
                    holes++;
                }
            }
        }
        return holes;
    }

    private int getBumpiness(Color[][] board) {
        int bumpiness = 0;
        int[] heights = getColumnHeights(board);
        for (int i = 0; i < heights.length - 1; i++) {
            bumpiness += Math.abs(heights[i] - heights[i + 1]);
        }
        return bumpiness;
    }

    private int[] getColumnHeights(Color[][] board) {
        int[] heights = new int[Board.getBoardWidth()];
        for (int c = 0; c < Board.getBoardWidth(); c++) {
            for (int r = 0; r < Board.getBoardHeight(); r++) {
                if (board[r][c] != null) {
                    heights[c] = Board.getBoardHeight() - r;
                    break;
                }
            }
        }
        return heights;
    }

    private Color[][] copyBoard(Color[][] original) {
        Color[][] copy = new Color[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}