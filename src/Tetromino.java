import java.awt.*;

public enum Tetromino {
    I_SHAPE(new int[][]{{1, 1, 1, 1}}, new Color(0, 240, 240)),
    J_SHAPE(new int[][]{{1, 0, 0}, {1, 1, 1}}, new Color(0, 0, 240)),
    L_SHAPE(new int[][]{{0, 0, 1}, {1, 1, 1}}, new Color(240, 160, 0)),
    O_SHAPE(new int[][]{{1, 1}, {1, 1}}, new Color(240, 240, 0)),
    S_SHAPE(new int[][]{{0, 1, 1}, {1, 1, 0}}, new Color(0, 240, 0)),
    T_SHAPE(new int[][]{{0, 1, 0}, {1, 1, 1}}, new Color(160, 0, 240)),
    Z_SHAPE(new int[][]{{1, 1, 0}, {0, 1, 1}}, new Color(240, 0, 0));

    public final int[][] shape;
    public final Color color;
    private int currentRotation;
    private int[][][] rotations;

    Tetromino(int[][] shape, Color color) {
        this.shape = shape;
        this.color = color;
        this.currentRotation = 0;
        generateRotations();
    }

    private void generateRotations() {
        rotations = new int[4][][];
        rotations[0] = shape;

        int[][] currentShape = shape;
        for (int i = 1; i < 4; i++) {
            currentShape = rotateMatrix(currentShape);
            rotations[i] = currentShape;
        }
    }

    private int[][] rotateMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] newMatrix = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                newMatrix[j][rows - 1 - i] = matrix[i][j];
            }
        }
        return newMatrix;
    }

    public int[][] getCurrentShape() {
        return rotations[currentRotation];
    }

    public int[][] getShapeForRotation(int rotationIndex) {
        if (rotationIndex < 0 || rotationIndex >= 4) {
            rotationIndex = (rotationIndex % 4 + 4) % 4;
        }
        return rotations[rotationIndex];
    }

    public void setRotation(int rotation) {
        this.currentRotation = rotation % 4;
    }

    public int getRotation() {
        return currentRotation;
    }

    public static Tetromino getRandomPiece() {
        return values()[(int) (Math.random() * values().length)];
    }
}