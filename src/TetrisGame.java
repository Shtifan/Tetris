import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class TetrisGame extends JFrame {
    private final int ROWS = 20;
    private final int COLS = 10;
    private final int TILE_SIZE = 30;
    private TetrisFigure currentPiece, holdPiece, nextPiece;
    private ArrayList<TetrisFigure> upcomingPieces;
    private Color[][] board;
    private boolean gamePaused = false;
    private Timer timer;
    private JPanel gamePanel, sidePanel;

    public TetrisGame() {
        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initializeGame();
    }

    private void initializeGame() {
        board = new Color[ROWS][COLS];
        upcomingPieces = new ArrayList<>();
        generateNextPieces();

        currentPiece = nextPiece;
        spawnNextPiece();

        // Game Panel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPiece(g, currentPiece);
            }
        };
        gamePanel.setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel, BorderLayout.CENTER);

        // Side Panel
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(150, ROWS * TILE_SIZE));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        add(sidePanel, BorderLayout.EAST);

        // Buttons
        JButton startButton = new JButton("Start");
        JButton pauseButton = new JButton("Pause");
        JButton newGameButton = new JButton("New Game");

        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> pauseGame());
        newGameButton.addActionListener(e -> newGame());

        sidePanel.add(startButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(pauseButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(newGameButton);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gamePaused) {
                    handleKeyPress(e);
                }
            }
        });
        setFocusable(true);

        pack();
        setLocationRelativeTo(null);
    }

    private void startGame() {
        timer = new Timer(500, e -> gameTick());
        timer.start();
    }

    private void pauseGame() {
        if (timer != null) {
            if (gamePaused) {
                timer.start();
            } else {
                timer.stop();
            }
            gamePaused = !gamePaused;
        }
    }

    private void newGame() {
        board = new Color[ROWS][COLS];
        upcomingPieces.clear();
        generateNextPieces();
        spawnNextPiece();
        holdPiece = null;
        repaint();
        startGame();
    }

    private void generateNextPieces() {
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            upcomingPieces.add(generateRandomPiece(random));
        }
        nextPiece = upcomingPieces.remove(0);
    }

    private TetrisFigure generateRandomPiece(Random random) {
        int pieceType = random.nextInt(7); // Assuming you have 7 subclasses for Tetris pieces
        // Replace the following lines with actual Tetris piece subclasses:
        switch (pieceType) {
            case 0:
                return new LFigure();
            case 1:
                return new ReverseLFigure();
            case 2:
                return new LFigure();
            case 3:
                return new ReverseZFigure();
            case 4:
                return new SquareFigure();
            case 5:
                return new LFigure();
            case 6:
                return new LFigure();
            default:
                return new LFigure();
        }
    }

    private void spawnNextPiece() {
        currentPiece = nextPiece;
        currentPiece.setPosition(COLS / 2 - 1, 0);
        if (upcomingPieces.isEmpty()) {
            generateNextPieces();
        }
        nextPiece = upcomingPieces.remove(0);
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] != null) {
                    g.setColor(board[row][col]);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPiece(Graphics g, TetrisFigure piece) {
        if (piece == null) return;
        int[][] shape = piece.getShape();
        g.setColor(piece.getColor());
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int x = (piece.getX() + col) * TILE_SIZE;
                    int y = (piece.getY() + row) * TILE_SIZE;
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> movePiece(-1, 0);
            case KeyEvent.VK_RIGHT -> movePiece(1, 0);
            case KeyEvent.VK_DOWN -> movePiece(0, 1);
            case KeyEvent.VK_UP -> currentPiece.rotate();
            case KeyEvent.VK_SPACE -> dropPiece();
            case KeyEvent.VK_C -> holdCurrentPiece();
        }
        repaint();
    }

    private void movePiece(int dx, int dy) {
        // Add collision detection here
        currentPiece.setPosition(currentPiece.getX() + dx, currentPiece.getY() + dy);
    }

    private void dropPiece() {
        // Add logic for fast dropping a piece
    }

    private void holdCurrentPiece() {
        if (holdPiece == null) {
            holdPiece = currentPiece;
            spawnNextPiece();
        } else {
            TetrisFigure temp = currentPiece;
            currentPiece = holdPiece;
            holdPiece = temp;
        }
    }

    private void gameTick() {
        movePiece(0, 1);
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
        });
    }
}
