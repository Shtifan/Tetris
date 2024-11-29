import java.awt.Color;

class SquareFigure extends TetrisFigure {
    @Override
    protected void initializeShape() {
        shape = new int[][]{
                {1, 1},
                {1, 1}
        };
    }

    @Override
    protected void initializeColor() {
        color = Color.YELLOW;
    }

    @Override
    public void rotate() {
    }
}