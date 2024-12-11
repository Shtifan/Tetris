import java.awt.Color;

class ReverseZPiece extends TetrisPiece {
    @Override
    protected void initializeShape() {
        shape = new int[][] {
                { 0, 1, 1 },
                { 1, 1, 0 },
                { 0, 0, 0 }
        };
    }

    @Override
    protected void initializeColor() {
        color = Color.GREEN;
    }
}
