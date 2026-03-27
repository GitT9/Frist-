import javax.swing.Icon;
import java.awt.*;

public class AppIcons {
    // Just a container file for icon classes
}

class EraserIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.rotate(Math.toRadians(45), 12, 12);
        
        g2.setColor(new Color(255, 150, 150)); // Pinkish
        g2.fillRect(6, 4, 12, 8);
        
        g2.setColor(Color.WHITE);
        g2.fillRect(6, 12, 12, 8);
        
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(6, 4, 12, 16);
        
        g2.dispose();
    }
    @Override public int getIconWidth() { return 24; }
    @Override public int getIconHeight() { return 24; }
}

class ZoomIcon implements Icon {
    private boolean isZoomIn;

    public ZoomIcon(boolean isZoomIn) {
        this.isZoomIn = isZoomIn;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));

        // Horizontal line
        g2.drawLine(8, 12, 16, 12);

        if (isZoomIn) {
            // Vertical line for '+'
            g2.drawLine(12, 8, 12, 16);
        }
        g2.dispose();
    }

    @Override public int getIconWidth() { return 24; }
    @Override public int getIconHeight() { return 24; }
}

class LayerIcon implements Icon {
    private boolean isAdd;

    public LayerIcon(boolean isAdd) {
        this.isAdd = isAdd;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.5f));

        // Draw a simple layer stack icon
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(6, 10, 12, 8);
        g2.drawRect(8, 6, 12, 8);

        // Draw plus or minus
        if (isAdd) {
            g2.setColor(PixelGridApp.ACCENT_COLOR.brighter());
            g2.drawLine(18, 10, 18, 14); // Vertical
            g2.drawLine(16, 12, 20, 12); // Horizontal
        } else {
            g2.setColor(Color.PINK);
            g2.drawLine(16, 12, 20, 12); // Horizontal only
        }

        g2.dispose();
    }

    @Override public int getIconWidth() { return 24; }
    @Override public int getIconHeight() { return 24; }
}

class FillBucketIcon implements Icon {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] xPoints = {5, 19, 17, 7};
        int[] yPoints = {6, 6, 19, 19};
        g2.setColor(new Color(100, 180, 255));
        g2.fillPolygon(xPoints, yPoints, 4);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawPolygon(xPoints, yPoints, 4);
        g2.drawArc(5, 2, 14, 10, 0, 180);
        g2.dispose();
    }
    @Override public int getIconWidth() { return 24; }
    @Override public int getIconHeight() { return 24; }
}