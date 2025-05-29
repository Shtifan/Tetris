import javax.swing.*;
import java.awt.*;

public class Tetris extends JFrame {

    private Board board;
    private JButton autoplayButton;

    public Tetris() {
        initUI();
    }

    private void initUI() {
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        board = new Board();

        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JLabel linesLabel = new JLabel("Lines: 0");
        linesLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel nextPiecePanel = getJPanel();

        board.setUiElements(scoreLabel, linesLabel, nextPiecePanel);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            board.start();
            board.requestFocusInWindow();
        });

        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            board.pause();
            board.requestFocusInWindow();
        });

        autoplayButton = new JButton("Autoplay: OFF");
        autoplayButton.addActionListener(e -> {
            board.toggleAutoplay();
            if (autoplayButton.getText().contains("OFF")) {
                autoplayButton.setText("Autoplay: ON");
            } else {
                autoplayButton.setText("Autoplay: OFF");
            }
            board.requestFocusInWindow();
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(board, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(150, board.getPreferredSize().height));

        JLabel nextPieceLabel = new JLabel("Next Piece:");
        nextPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        linesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        autoplayButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidePanel.add(nextPieceLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(nextPiecePanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(scoreLabel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(linesLabel);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(startButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(pauseButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sidePanel.add(autoplayButton);


        mainPanel.add(sidePanel, BorderLayout.EAST);
        add(mainPanel);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel getJPanel() {
        JPanel nextPiecePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (board != null) {
                    board.drawNextPiece(g, this);
                }
            }
        };
        nextPiecePanel.setPreferredSize(new Dimension(4 * 30, 4 * 30));
        nextPiecePanel.setBackground(Color.DARK_GRAY);
        nextPiecePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return nextPiecePanel;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}