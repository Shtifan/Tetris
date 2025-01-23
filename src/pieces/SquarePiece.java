package pieces;

import java.awt.Color;

import base.TetrisPiece;

public class SquarePiece extends TetrisPiece {
    @Override
    protected void initializeShape() {
        shape = new int[][]{{1, 1}, {1, 1}};
    }

    @Override
    protected void initializeColor() {
        color = Color.YELLOW;
    }
}