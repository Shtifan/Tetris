import java.awt.Color;

class ReverseLFigure extends TetrisFigure {
    @Override
    protected void initializeShape() {
        shape = new int[][]{
                {0, 0, 1},
                {1, 1, 1},
                {0, 0, 0}
        };
    }

    @Override
    protected void initializeColor() {
        color = Color.BLUE;
    }
}