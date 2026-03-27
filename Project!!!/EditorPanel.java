import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class EditorPanel extends JPanel {
    private PixelGridApp app;
    private PixelCanvas canvas;
    private JScrollPane scrollPane;
    private Color currentColor = Color.BLACK;
    private boolean isEraser = false;
    private JLabel zoomLabel;
    private JPanel layerListPanel;
   
    public EditorPanel(PixelGridApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        // Toolbar (Top)
        add(createTopBar(), BorderLayout.NORTH);
        
        // Sidebar (Left) - Tools & Colors
        add(createSideBar(), BorderLayout.WEST);

        // Center Area
        JPanel centerArea = new JPanel(new GridBagLayout()); // Centers the canvas
        centerArea.setBackground(PixelGridApp.BG_COLOR);
        
        canvas = new PixelCanvas(32, 32, this);
        scrollPane = new JScrollPane(centerArea);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.addMouseWheelListener(e -> {
            // Negative rotation is scroll up (zoom in), positive is scroll down (zoom out)
            updateZoom(-e.getWheelRotation() * 0.1);
        });

        canvas.setLayerUpdateCallback(this::updateLayerList); // Connect callback

        // Style the scrollbars
        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());
        
        // Add canvas to center area
        centerArea.add(canvas);
        updateLayerList(); // Initial layer UI
        
        add(scrollPane, BorderLayout.CENTER);
    }

    public void initCanvas(int w, int h) {
        canvas.reset(w, h);
        revalidate();
        repaint();
    }

    public boolean loadCanvas(File file) {
        boolean success = canvas.loadFromFile(file);
        if (success) updateLayerList();
        return success;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(PixelGridApp.PANEL_COLOR);
        bar.setBorder(new EmptyBorder(5, 10, 5, 10));

        ModernButton homeBtn = new ModernButton("Home", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        // homeBtn.setIcon(new ImageIcon("path/to/home_icon.png"));
        homeBtn.addActionListener(e -> app.showWelcome());
        
        ModernButton undoBtn = new ModernButton("Undo", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        undoBtn.addActionListener(e -> canvas.undo());
        
        ModernButton redoBtn = new ModernButton("Redo", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        redoBtn.addActionListener(e -> canvas.redo());
        
        ModernButton saveBtn = new ModernButton("Save", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        // saveBtn.setIcon(new ImageIcon("path/to/save_icon.png"));
        saveBtn.addActionListener(e -> saveProject());

        ModernButton exportBtn = new ModernButton("Export", PixelGridApp.ACCENT_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        // exportBtn.setIcon(new ImageIcon("path/to/export_icon.png"));
        exportBtn.addActionListener(e -> showExportDialog());

        // Zoom Controls
        JButton zoomOutBtn = new ModernButton("", PixelGridApp.PANEL_COLOR.brighter(), Color.WHITE);
        zoomOutBtn.setIcon(new ZoomIcon(false));
        zoomOutBtn.setPreferredSize(new Dimension(40, 30));
        zoomOutBtn.addActionListener(e -> updateZoom(-0.1));

        zoomLabel = new JLabel("100%");
        zoomLabel.setForeground(Color.WHITE);
        zoomLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        zoomLabel.setPreferredSize(new Dimension(50, 30));
        zoomLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton zoomInBtn = new ModernButton("", PixelGridApp.PANEL_COLOR.brighter(), Color.WHITE);
        zoomInBtn.setIcon(new ZoomIcon(true));
        zoomInBtn.setPreferredSize(new Dimension(40, 30));
        zoomInBtn.addActionListener(e -> updateZoom(0.1));

        bar.add(homeBtn);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(undoBtn);
        bar.add(redoBtn);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(saveBtn);
        bar.add(exportBtn);
        bar.add(Box.createHorizontalStrut(20));
        bar.add(zoomOutBtn);
        bar.add(zoomLabel);
        bar.add(zoomInBtn);
        
        return bar;
    }

    void updateZoom(double delta) {
        double current = canvas.getZoom();
        double next = Math.max(0.1, Math.min(5.0, current + delta));
        canvas.setZoom(next);
        zoomLabel.setText(String.format("%d%%", (int)(next * 100)));
    }

    private JPanel createSideBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PixelGridApp.PANEL_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(130, 0));

        // --- Tools Section ---
        panel.add(createSectionLabel("Tools"));
        panel.add(Box.createVerticalStrut(10));

        // Color & Hex Container
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setBackground(PixelGridApp.PANEL_COLOR);
        colorPanel.setMaximumSize(new Dimension(120, 40));
        colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Color Button (Flat Style - No white borders)
        JButton colorPickerButton = new JButton();
        colorPickerButton.setBackground(currentColor);
        colorPickerButton.setPreferredSize(new Dimension(40, 40));
        colorPickerButton.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Make it flat
        colorPickerButton.setBorderPainted(false);
        colorPickerButton.setFocusPainted(false);
        colorPickerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hex Input Field
        JTextField hexField = new JTextField(String.format("#%06X", (0xFFFFFF & currentColor.getRGB())));
        hexField.setFont(new Font("Monospaced", Font.BOLD, 12));
        hexField.setForeground(Color.WHITE);
        hexField.setBackground(PixelGridApp.BG_COLOR);
        hexField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        hexField.setCaretColor(Color.WHITE);
        
        JToggleButton eraserToggle = new JToggleButton("Eraser");
        styleToggle(eraserToggle);
        
        JToggleButton fillToggle = new JToggleButton("Fill Bucket");
        styleToggle(fillToggle);

        eraserToggle.setIcon(new EraserIcon());
        fillToggle.setIcon(new FillBucketIcon());

        // Actions
        ActionListener updateColorAction = e -> {
            try {
                String text = hexField.getText().trim();
                if (!text.startsWith("#")) text = "#" + text;
                Color c = Color.decode(text);
                currentColor = c;
                canvas.setCurrentColor(c);
                colorPickerButton.setBackground(c);
                if (eraserToggle.isSelected()) {
                    eraserToggle.setSelected(false);
                    canvas.setEraser(false);
                }
                // Note: We don't disable Fill when picking color, 
                // so user can pick a new color and fill immediately.
            } catch (NumberFormatException ex) {
                // Ignore invalid hex
            }
        };
        hexField.addActionListener(updateColorAction);
        hexField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) { updateColorAction.actionPerformed(null); }
        });

        colorPickerButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Drawing Color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
                canvas.setCurrentColor(newColor);
                colorPickerButton.setBackground(newColor);
                hexField.setText(String.format("#%06X", (0xFFFFFF & newColor.getRGB())));
                if (eraserToggle.isSelected()) {
                    eraserToggle.setSelected(false);
                    canvas.setEraser(false);
                }
                // Note: We don't disable Fill when picking color.
            }
        });

        colorPanel.add(colorPickerButton, BorderLayout.WEST);
        colorPanel.add(hexField, BorderLayout.CENTER);
        
        panel.add(colorPanel);
        panel.add(Box.createVerticalStrut(10));

        // Toggle Logic: Ensure Eraser and Fill are mutually exclusive
        eraserToggle.addActionListener(e -> {
            boolean selected = eraserToggle.isSelected();
            canvas.setEraser(selected);
            if (selected) {
                fillToggle.setSelected(false);
                canvas.setFill(false);
            }
        });

        fillToggle.addActionListener(e -> {
            boolean selected = fillToggle.isSelected();
            canvas.setFill(selected);
            if (selected) {
                eraserToggle.setSelected(false);
                canvas.setEraser(false);
            }
        });

        panel.add(eraserToggle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(fillToggle);

        // --- Canvas Section ---
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSectionLabel("Canvas"));
        panel.add(Box.createVerticalStrut(10));

        // Layer Controls
        JPanel layerControlPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        layerControlPanel.setBackground(PixelGridApp.PANEL_COLOR);
        layerControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton addLayerBtn = new ModernButton("", PixelGridApp.ACCENT_COLOR, Color.BLACK);
        addLayerBtn.setIcon(new LayerIcon(true));
        addLayerBtn.setToolTipText("Add Layer");
        addLayerBtn.addActionListener(e -> canvas.addLayer());
        
        JButton delLayerBtn = new ModernButton("", Color.RED.darker(), Color.WHITE);
        delLayerBtn.setIcon(new LayerIcon(false));
        delLayerBtn.setToolTipText("Delete Layer");
        delLayerBtn.addActionListener(e -> canvas.removeLayer());
        
        layerControlPanel.add(addLayerBtn);
        layerControlPanel.add(delLayerBtn);
        panel.add(layerControlPanel);
        panel.add(Box.createVerticalStrut(5));

        // Layer List Container
        layerListPanel = new JPanel();
        layerListPanel.setLayout(new BoxLayout(layerListPanel, BoxLayout.Y_AXIS));
        layerListPanel.setBackground(PixelGridApp.PANEL_COLOR);
        layerListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane layerScroll = new JScrollPane(layerListPanel);
        layerScroll.setPreferredSize(new Dimension(120, 150));
        layerScroll.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        layerScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        panel.add(layerScroll);
        panel.add(Box.createVerticalStrut(10));

        // Grid Size Selector
        String[] sizes = {"16x16", "32x32", "64x64"};
        JComboBox<String> gridSizeSelector = new JComboBox<>(sizes);
        gridSizeSelector.setSelectedIndex(1);
        gridSizeSelector.setMaximumSize(new Dimension(120, 30));
        gridSizeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        gridSizeSelector.addActionListener(e -> {
            String selected = (String) gridSizeSelector.getSelectedItem();
            int newSize = Integer.parseInt(selected.split("x")[0]);
            canvas.reset(newSize, newSize);
        });
        panel.add(gridSizeSelector);
        panel.add(Box.createVerticalStrut(5));

        // Clear Button
        JButton clearButton = new ModernButton("Clear", PixelGridApp.BUTTON_COLOR, PixelGridApp.BUTTON_TEXT_COLOR);
        clearButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearButton.addActionListener(e -> canvas.clearCanvas());
        panel.add(clearButton);

        return panel;
    }

    private void updateLayerList() {
        layerListPanel.removeAll();
        List<Layer> layers = canvas.getLayers();
        int activeIdx = canvas.getActiveLayerIndex();

        // Render layers in reverse order (top layer first in UI)
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            final int idx = i;
            
            JPanel item = new JPanel(new BorderLayout());
            item.setMaximumSize(new Dimension(200, 30));
            item.setBackground(i == activeIdx ? PixelGridApp.ACCENT_COLOR.darker() : PixelGridApp.PANEL_COLOR.brighter());
            item.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            
            JCheckBox visCheck = new JCheckBox();
            visCheck.setSelected(layer.isVisible());
            visCheck.setOpaque(false);
            visCheck.addActionListener(e -> canvas.toggleLayerVisibility(idx));
            
            JLabel nameLabel = new JLabel(layer.getName());
            nameLabel.setForeground(Color.WHITE);
            nameLabel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { canvas.setActiveLayer(idx); }
            });

            item.add(visCheck, BorderLayout.WEST);
            item.add(nameLabel, BorderLayout.CENTER);
            layerListPanel.add(item);
            layerListPanel.add(Box.createVerticalStrut(2));
        }
        layerListPanel.revalidate();
        layerListPanel.repaint();
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.LIGHT_GRAY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new ModernScrollBarUI());
        scrollBar.setPreferredSize(new Dimension(10, 10));
        scrollBar.setBackground(PixelGridApp.PANEL_COLOR);
    }

    private void styleToggle(JToggleButton btn) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(120, 40));
    }

    private void saveProject() {
        JFileChooser ch = new JFileChooser();
        ch.setFileFilter(new FileNameExtensionFilter("PixelGrid Project (.pxg)", "pxg"));
        if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            if (!f.getName().endsWith(".pxg")) f = new File(f.getAbsolutePath() + ".pxg");
            canvas.saveToFile(f);
        }
    }

    private void showExportDialog() {
        // Custom panel for export options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // Format selector
        String[] formats = {"PNG", "JPG"};
        JComboBox<String> formatComboBox = new JComboBox<>(formats);
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formatPanel.add(new JLabel("Format:"));
        formatPanel.add(formatComboBox);
        formatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Checkboxes
        JCheckBox gridCheckBox = new JCheckBox("Include Grid Lines");
        JCheckBox transparentBgCheckBox = new JCheckBox("Transparent Background (PNG only)");

        optionsPanel.add(formatPanel);
        optionsPanel.add(gridCheckBox);
        optionsPanel.add(transparentBgCheckBox);

        // Disable transparency option for JPG
        formatComboBox.addActionListener(e -> {
            boolean isPng = "PNG".equals(formatComboBox.getSelectedItem());
            transparentBgCheckBox.setEnabled(isPng);
            if (!isPng) {
                transparentBgCheckBox.setSelected(false);
            }
        });

        int dialogResult = JOptionPane.showConfirmDialog(this, optionsPanel, "Export Options", JOptionPane.OK_CANCEL_OPTION);

        if (dialogResult == JOptionPane.OK_OPTION) {
            String format = (String) formatComboBox.getSelectedItem();
            boolean includeGrid = gridCheckBox.isSelected();
            boolean transparentBg = transparentBgCheckBox.isSelected();

            JFileChooser ch = new JFileChooser();
            ch.setFileFilter(new FileNameExtensionFilter(format + " Image", format.toLowerCase()));
            if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = ch.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith("." + format.toLowerCase())) {
                    f = new File(f.getAbsolutePath() + "." + format.toLowerCase());
                }
                canvas.exportImage(f, format, includeGrid, transparentBg);
            }
        }
    }
}