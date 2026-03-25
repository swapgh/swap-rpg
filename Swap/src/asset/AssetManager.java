package asset;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public final class AssetManager {
    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Map<String, String[]> animationClips = new HashMap<>();
    private final Map<String, Font> fonts = new HashMap<>();

    public void loadImage(String id, String resourcePath, int width, int height) {
        images.put(id, loadAndScale(resourcePath, width, height));
    }

    public BufferedImage image(String id) {
        BufferedImage image = images.get(id);
        if (image == null) {
            throw new IllegalArgumentException("Missing image: " + id);
        }
        return image;
    }

    public void registerClip(String id, String... frameIds) {
        animationClips.put(id, frameIds);
    }

    public String[] clip(String id) {
        String[] frames = animationClips.get(id);
        if (frames == null) {
            throw new IllegalArgumentException("Missing animation clip: " + id);
        }
        return frames;
    }

    public void loadFont(String id, String resourcePath, float size) {
        try (InputStream is = AssetManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing font resource: " + resourcePath);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            fonts.put(id, font);
        } catch (FontFormatException | IOException ex) {
            throw new IllegalStateException("Unable to load font " + resourcePath, ex);
        }
    }

    public Font font(String id) {
        Font font = fonts.get(id);
        if (font == null) {
            throw new IllegalArgumentException("Missing font: " + id);
        }
        return font;
    }

    private static BufferedImage loadAndScale(String resourcePath, int width, int height) {
        try (InputStream is = AssetManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing image resource: " + resourcePath);
            }
            BufferedImage raw = ImageIO.read(is);
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(raw, 0, 0, width, height, null);
            g2.dispose();
            return scaled;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load image " + resourcePath, ex);
        }
    }
}
