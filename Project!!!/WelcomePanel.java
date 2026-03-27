import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    private PixelGridApp app;

    public WelcomePanel(PixelGridApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(PixelGridApp.BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("PixelGrid");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        gbc.gridy++;
        ModernButton newBtn = new ModernButton("New Project", PixelGridApp.ACCENT_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        newBtn.setPreferredSize(new Dimension(350, 80));
        newBtn.addActionListener(e -> showNewDialog());
        add(newBtn, gbc);

        gbc.gridy++;
        ModernButton loadBtn = new ModernButton("Load Project", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        loadBtn.setPreferredSize(new Dimension(350, 80));
        loadBtn.addActionListener(e -> showLoadDialog());
        add(loadBtn, gbc);
    }

    private void showNewDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField wField = new JTextField("32");
        JTextField hField = new JTextField("32");
        panel.add(new JLabel("Width:")); panel.add(wField);
        panel.add(new JLabel("Height:")); panel.add(hField);

        int result = JOptionPane.showConfirmDialog(this, panel, "New Canvas Size", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int w = Integer.parseInt(wField.getText());
                int h = Integer.parseInt(hField.getText());
                app.startNewProject(w, h);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format");
            }
        }
    }

    private void showLoadDialog() {
        JFileChooser ch = new JFileChooser();
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("PixelGrid Project (.pxg)", "pxg");
        ch.setFileFilter(filter);
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            app.loadProject(ch.getSelectedFile());
        }
    }
}