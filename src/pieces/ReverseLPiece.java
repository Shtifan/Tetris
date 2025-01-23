package pieces;

import java.awt.Color;

import base.TetrisPiece;

public class ReverseLPiece extends TetrisPiece {
    @Override
    protected void initializeShape() {
        shape = new int[][]{{0, 0, 1}, {1, 1, 1}, {0, 0, 0}};
    }

    @Override
    protected void initializeColor() {
        color = Color.BLUE;
    }
}