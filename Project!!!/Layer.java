import java.awt.Color;
import java.io.Serializable;

public class Layer implements Serializable {
    private String name;
    private boolean visible;
    private Color[][] pixels;

    public Layer(String name, int w, int h) {
        this.name = name;
        this.visible = true;
        this.pixels = new Color[h][w];
        // Initialize with null (transparent) instead of White
    }

    // Copy constructor for Undo/Redo
    public Layer(Layer other) {
        this.name = other.name;
        this.visible = other.visible;
        this.pixels = new Color[other.pixels.length][other.pixels[0].length];
        for (int i = 0; i < pixels.length; i++) {
            System.arraycopy(other.pixels[i], 0, this.pixels[i], 0, pixels[0].length);
        }
    }

    public String getName() { return name; }
    public boolean isVisible() { return visible; }
    public void toggleVisible() { this.visible = !this.visible; }
    public Color[][] getPixels() { return pixels; }
    public void setPixels(Color[][] p) { this.pixels = p; }
    public Color getPixel(int x, int y) { return pixels[y][x]; }
    public void setPixel(int x, int y, Color c) { pixels[y][x] = c; }
    public void clear() {
        for(int i=0; i<pixels.length; i++) 
            for(int j=0; j<pixels[0].length; j++) pixels[i][j] = null;
    }
}