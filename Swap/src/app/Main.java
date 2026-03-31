package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame(GameConfig.WINDOW_TITLE);
            GamePanel panel = new GamePanel();
            window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            window.setResizable(false);
            window.add(panel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    panel.shutdown();
                    window.dispose();
                    System.exit(0);
                }
            });
            window.setVisible(true);
            panel.start();
        });
    }
}
