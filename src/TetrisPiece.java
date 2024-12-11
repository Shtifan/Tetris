package base;

import java.awt.Color;

public abstract class TetrisPiece {
    protected int[][] shape;
    protected Color color;
    protected int x, y;

    public TetrisPiece() {
        initializeShape();
        initializeColor();
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
}