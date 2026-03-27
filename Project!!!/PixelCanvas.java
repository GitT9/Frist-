import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class PixelCanvas extends JPanel implements MouseListener, MouseMotionListener {
    private int gridW, gridH;
    private static final int BASE_PIXEL_SIZE = 20;
    private double zoom = 1.0;
    
    private List<Layer> layers;
    private int activeLayerIndex = 0;
    
    private Color currentColor = Color.BLACK;
    private boolean isEraser = false;
    private boolean isFill = false;
    private EditorPanel editorPanel; // Reference to parent panel
    private Runnable layerUpdateCallback;
    
    // Undo/Redo Stacks
    private Stack<List<Layer>> undoStack = new Stack<>();
    private Stack<List<Layer>> redoStack = new Stack<>();

    public PixelCanvas(int w, int h, EditorPanel editor) {
        this.editorPanel = editor;
        reset(w, h);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        // Keyboard Shortcuts for Undo (Ctrl+Z) and Redo (Ctrl+Y)
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
        am.put("Undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { undo(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo");
        am.put("Redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { redo(); }
        });

        // Keyboard Shortcuts for Zoom (+/-)
        Action zoomInAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (editorPanel != null) editorPanel.updateZoom(0.1);
            }
        };
        Action zoomOutAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (editorPanel != null) editorPanel.updateZoom(-0.1);
            }
        };
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "zoomIn");
        am.put("zoomIn", zoomInAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "zoomOut");
        am.put("zoomOut", zoomOutAction);
    }

    public void setLayerUpdateCallback(Runnable r) { this.layerUpdateCallback = r; }
    public List<Layer> getLayers() { return layers; }
    public int getActiveLayerIndex() { return activeLayerIndex; }
    public double getZoom() { return zoom; }

    public void setZoom(double z) {
        this.zoom = z;
        updateSize();
        repaint();
    }

    private int getPixelSize() {
        return (int)(BASE_PIXEL_SIZE * zoom);
    }

    public void addLayer() {
        pushUndo();
        layers.add(new Layer("Layer " + (layers.size() + 1), gridW, gridH));
        activeLayerIndex = layers.size() - 1;
        if (layerUpdateCallback != null) layerUpdateCallback.run();
        repaint();
    }

    public void removeLayer() {
        if (layers.size() <= 1) return; // Keep at least one layer
        pushUndo();
        layers.remove(activeLayerIndex);
        if (activeLayerIndex >= layers.size()) activeLayerIndex = layers.size() - 1;
        if (layerUpdateCallback != null) layerUpdateCallback.run();
        repaint();
    }

    public void setActiveLayer(int index) {
        if (index >= 0 && index < layers.size()) {
            this.activeLayerIndex = index;
            if (layerUpdateCallback != null) layerUpdateCallback.run();
            repaint();
        }
    }

    public void toggleLayerVisibility(int index) {
        if (index >= 0 && index < layers.size()) {
            layers.get(index).toggleVisible();
            repaint();
        }
    }

    public void clearCanvas() {
        pushUndo();
        layers.get(activeLayerIndex).clear();
        repaint();
    }

    public void reset(int w, int h) {
        this.gridW = w;
        this.gridH = h;
        this.layers = new ArrayList<>();
        this.layers.add(new Layer("Layer 1", w, h));
        this.activeLayerIndex = 0;
        
        undoStack.clear();
        redoStack.clear();
        updateSize();
        if (layerUpdateCallback != null) layerUpdateCallback.run();
        repaint();
    }

    private void updateSize() {
        setPreferredSize(new Dimension(gridW * getPixelSize(), gridH * getPixelSize()));
        revalidate();
    }

    public void setCurrentColor(Color c) { this.currentColor = c; }
    public void setEraser(boolean e) { this.isEraser = e; }
    public void setFill(boolean f) { this.isFill = f; }

    private void pushUndo() {
        List<Layer> state = new ArrayList<>();
        for (Layer l : layers) {
            state.add(new Layer(l)); // Deep copy
        }
        undoStack.push(state);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new ArrayList<>(layers)); // Save current
            layers = undoStack.pop();
            // Ensure active index is valid
            if (activeLayerIndex >= layers.size()) activeLayerIndex = layers.size() - 1;
            if (layerUpdateCallback != null) layerUpdateCallback.run();
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(new ArrayList<>(layers));
            layers = redoStack.pop();
            if (activeLayerIndex >= layers.size()) activeLayerIndex = layers.size() - 1;
            if (layerUpdateCallback != null) layerUpdateCallback.run();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int ps = getPixelSize();

        // Draw solid white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw Layers
        for (Layer layer : layers) {
            if (!layer.isVisible()) continue;
            Color[][] pixels = layer.getPixels();

            for (int i = 0; i < gridH; i++) {
                for (int j = 0; j < gridW; j++) {
                    Color c = pixels[i][j];
                    if (c != null) { // Only draw non-null pixels
                        g2d.setColor(c);
                        g2d.fillRect(j * ps, i * ps, ps, ps);
                    }
                }
            }
        }
        
        // Draw Grid
        g2d.setColor(PixelGridApp.GRID_LINE_COLOR);
        for (int i = 0; i < gridH; i++) {
            for (int j = 0; j < gridW; j++) {
                g2d.drawRect(j * ps, i * ps, ps, ps);
            }
        }
    }

    private void paintPixel(int x, int y) {
        int ps = getPixelSize();
        int col = x / ps;
        int row = y / ps;

        if (row >= 0 && row < gridH && col >= 0 && col < gridW) {
            // Eraser makes pixel null (transparent), Pen sets color
            layers.get(activeLayerIndex).setPixel(col, row, isEraser ? null : currentColor);
            repaint();
        }
    }

    private void fillArea(int x, int y) {
        int ps = getPixelSize();
        int col = x / ps;
        int row = y / ps;
        
        if (row >= 0 && row < gridH && col >= 0 && col < gridW) {
            Color targetColor = layers.get(activeLayerIndex).getPixel(col, row);
            Color replacementColor = isEraser ? null : currentColor;
            
            boolean same = (targetColor == null && replacementColor == null) || 
                           (targetColor != null && targetColor.equals(replacementColor));
            
            if (!same) {
                floodFill(col, row, targetColor, replacementColor);
                repaint();
            }
        }
    }

    private void floodFill(int x, int y, Color target, Color replace) {
        // target can be null, replace can be null
        Color[][] pixels = layers.get(activeLayerIndex).getPixels();

        boolean[][] visited = new boolean[gridH][gridW]; // Prevent reprocessing
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(x, y));
        visited[y][x] = true;
        
        while (!q.isEmpty()) {
            Point p = q.remove();
            pixels[p.y][p.x] = replace;
            
            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];

                if (nx >= 0 && nx < gridW && ny >= 0 && ny < gridH && !visited[ny][nx]) {
                    Color c = pixels[ny][nx];
                    boolean match = (target == null && c == null) || (target != null && target.equals(c));
                    if (match) {
                        visited[ny][nx] = true;
                        q.add(new Point(nx, ny));
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!layers.get(activeLayerIndex).isVisible()) return; // Don't draw on hidden layer
        pushUndo(); 
        if (isFill) {
            fillArea(e.getX(), e.getY());
        } else {
            paintPixel(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!layers.get(activeLayerIndex).isVisible()) return;
        if (!isFill) {
            paintPixel(e.getX(), e.getY());
        }
    }

    // File Operations
    public void saveToFile(File f) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeInt(gridW);
            oos.writeInt(gridH);
            oos.writeObject(layers);
            JOptionPane.showMessageDialog(this, "Project Saved!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    public boolean loadFromFile(File f) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            int w = ois.readInt();
            int h = ois.readInt();
            Object obj = ois.readObject();
            
            List<Layer> loadedLayers;
            if (obj instanceof Color[][]) {
                // Convert legacy save format
                loadedLayers = new ArrayList<>();
                Layer l = new Layer("Layer 1", w, h);
                l.setPixels((Color[][]) obj);
                loadedLayers.add(l);
            } else {
                loadedLayers = (List<Layer>) obj;
            }
            
            this.gridW = w;
            this.gridH = h;
            this.layers = loadedLayers;
            this.activeLayerIndex = 0;
            undoStack.clear();
            redoStack.clear();
            updateSize();
            repaint();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
            return false;
        }
    }

    public void exportImage(File f, String format, boolean includeGrid, boolean transparentBg) {
        int imageType = ("PNG".equalsIgnoreCase(format) && transparentBg)
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;

        // Export at 100% scale (base pixel size)
        BufferedImage image = new BufferedImage(gridW * BASE_PIXEL_SIZE, gridH * BASE_PIXEL_SIZE, imageType);
        Graphics2D g2d = image.createGraphics();

        if (imageType == BufferedImage.TYPE_INT_RGB) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        // Draw all visible layers
        for (Layer layer : layers) {
            if (!layer.isVisible()) continue;
            Color[][] pixels = layer.getPixels();
            for (int i = 0; i < gridH; i++) {
                for (int j = 0; j < gridW; j++) {
                    Color c = pixels[i][j];
                    if (c == null) continue;
                    
                    int x = j * BASE_PIXEL_SIZE;
                    int y = i * BASE_PIXEL_SIZE;
                    g2d.setColor(c);
                    g2d.fillRect(x, y, BASE_PIXEL_SIZE, BASE_PIXEL_SIZE);
                }
            }
        }
        
        if (includeGrid) {
            g2d.setColor(PixelGridApp.GRID_LINE_COLOR);
            for (int i = 0; i < gridH; i++) {
                for (int j = 0; j < gridW; j++) {
                    g2d.drawRect(j * BASE_PIXEL_SIZE, i * BASE_PIXEL_SIZE, BASE_PIXEL_SIZE, BASE_PIXEL_SIZE);
                }
            }
        }
        
        g2d.dispose();
        try {
            ImageIO.write(image, format, f);
            JOptionPane.showMessageDialog(this, "Exported Successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export Failed: " + e.getMessage());
        }
    }

    // Unused
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
}