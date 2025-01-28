package base;

import java.awt.Color;

public abstract class TetrisPiece implements Cloneable {
    protected int[][] shape;
    protected Color color;
    protected int x, y;
    protected int rotation;

    public TetrisPiece() {
        initializeShape();
        initializeColor();
        rotation = 0;
    }

    protected abstract void initializeShape();

    protected abstract void initializeColor();

    public void rotate() {
        int[][] rotatedShape = new int[shape.length][shape[0].length];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                rotatedShape[j][shape.length - 1 - i] = shape[i][j];
            }
        }

        shape = rotatedShape;
        rotation = (rotation + 1) % 4;
    }

    public int[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return shape[0].length;
    }

    public int getHeight() {
        return shape.length;
    }

    public int getSize() {
        return shape.length;
    }

    public int getShape(int row, int col) {
        return shape[row][col];
    }

    public TetrisPiece getRotated(int rotation) {
        TetrisPiece rotated = this.clone();
        for (int i = 0; i < rotation; i++) {
            rotated.rotate();
        }
        return rotated;
    }

    @Override
    protected TetrisPiece clone() {
        try {
            TetrisPiece clone = (TetrisPiece) super.clone();
            clone.shape = new int[shape.length][shape[0].length];
            for (int i = 0; i < shape.length; i++) {
                System.arraycopy(shape[i], 0, clone.shape[i], 0, shape[i].length);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public int getRotation() {
        return rotation;
    }
}
