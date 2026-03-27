import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModernButton extends JButton {
    private Color normalColor;
    private Color hoverColor;

    public ModernButton(String text, Color bg, Color fg) {
        super(text);
        this.normalColor = bg;
        this.hoverColor = bg.brighter();
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setBackground(bg);
        setForeground(fg);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false); // We'll paint our own background
        
        // Hover and Press Effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(normalColor.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { setBackground(normalColor); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
}