import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PixelGridApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private EditorPanel editorPanel;
    
    // --- Modern UI Theme ---
    public static final Color BG_COLOR = new Color(0x1E1E1E);
    public static final Color PANEL_COLOR = new Color(0x2C2C2C);
    public static final Color BUTTON_COLOR = new Color(0xE0E0E0);
    public static final Color ACCENT_COLOR = new Color(0x4CAF50);
    public static final Color GRID_LINE_COLOR = new Color(0x3A3A3A);
    public static final Color BUTTON_TEXT_COLOR = Color.BLACK;

    public static void main(String[] args) {
        // Properties for modern rendering
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new PixelGridApp().setVisible(true);
        });
    }

    public PixelGridApp() {
        setTitle("PixelGrid");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(new WelcomePanel(this), "WELCOME");
        editorPanel = new EditorPanel(this);
        mainContainer.add(editorPanel, "EDITOR");

        add(mainContainer);
    }

    public void startNewProject(int width, int height) {
        editorPanel.initCanvas(width, height);
        cardLayout.show(mainContainer, "EDITOR");
    }

    public void loadProject(File file) {
        if (editorPanel.loadCanvas(file)) {
            cardLayout.show(mainContainer, "EDITOR");
        }
    }

    public void showWelcome() {
        cardLayout.show(mainContainer, "WELCOME");
    }
}