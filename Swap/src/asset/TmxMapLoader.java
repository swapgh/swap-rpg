package asset;

import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Loader para mapas TMX de Tiled con tilesets embebidos e imagenes en un solo PNG.
 */
public final class TmxMapLoader {
    private static final long FLIPPED_HORIZONTALLY_FLAG = 0x80000000L;
    private static final long FLIPPED_VERTICALLY_FLAG = 0x40000000L;
    private static final long FLIPPED_DIAGONALLY_FLAG = 0x20000000L;
    private static final long ROTATED_HEXAGONAL_120_FLAG = 0x10000000L;
    private static final long ALL_FLIP_FLAGS = FLIPPED_HORIZONTALLY_FLAG
            | FLIPPED_VERTICALLY_FLAG
            | FLIPPED_DIAGONALLY_FLAG
            | ROTATED_HEXAGONAL_120_FLAG;

    public TileMap load(String resourcePath, AssetManager assets, int tileSize) {
        Element map = parseMap(resourcePath);
        int cols = integerAttribute(map, "width");
        int rows = integerAttribute(map, "height");
        int sourceTileWidth = integerAttribute(map, "tilewidth");
        int sourceTileHeight = integerAttribute(map, "tileheight");
        if (sourceTileWidth <= 0 || sourceTileHeight <= 0) {
            throw new IllegalArgumentException("Invalid TMX tile size: " + resourcePath);
        }

        List<TilesetSpec> tilesets = parseTilesets(map);
        TileDefinition[] definitions = registerTilesets(resourcePath, assets, tileSize, sourceTileWidth, sourceTileHeight, tilesets);
        List<LayerSpec> layers = parseLayers(map, cols, rows);
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("TMX map has no tile layers: " + resourcePath);
        }

        int[][][] layerTileIds = new int[layers.size()][][];
        for (int i = 0; i < layers.size(); i++) {
            layerTileIds[i] = layers.get(i).tileIds();
        }
        CollisionProfile collision = buildCollisionProfile(cols, rows, layers, tilesets, definitions, assets);
        BufferedImage[][] foregroundMasks = buildForegroundMasks(cols, rows, layers, tilesets, definitions, assets);
        return new TileMap(tileSize, cols, rows, layerTileIds, definitions, collision.blockedCells(), collision.blockedMasks(),
                foregroundMasks);
    }

    private Element parseMap(String resourcePath) {
        try (InputStream is = TmxMapLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing TMX resource: " + resourcePath);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(is);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new IllegalStateException("Unable to load TMX map " + resourcePath, ex);
        }
    }

    private List<TilesetSpec> parseTilesets(Element map) {
        List<TilesetSpec> tilesets = new ArrayList<>();
        for (Element tileset : childElements(map, "tileset")) {
            Element image = firstChild(tileset, "image");
            tilesets.add(new TilesetSpec(
                    integerAttribute(tileset, "firstgid"),
                    stringAttribute(tileset, "name"),
                    integerAttribute(tileset, "tilecount"),
                    integerAttribute(tileset, "columns"),
                    stringAttribute(image, "source")));
        }
        return List.copyOf(tilesets);
    }

    private TileDefinition[] registerTilesets(String resourcePath, AssetManager assets, int tileSize, int sourceTileWidth,
            int sourceTileHeight, List<TilesetSpec> tilesets) {
        int maxGid = 0;
        for (TilesetSpec tileset : tilesets) {
            maxGid = Math.max(maxGid, tileset.firstGid + tileset.tileCount - 1);
        }
        TileDefinition[] definitions = new TileDefinition[maxGid + 1];
        for (TilesetSpec tileset : tilesets) {
            if (tileset.columns <= 0) {
                throw new IllegalArgumentException("TMX tileset has invalid columns: " + resourcePath + " -> " + tileset.name);
            }
            boolean blocked = isSolidTileset(tileset.name);
            for (int localIndex = 0; localIndex < tileset.tileCount; localIndex++) {
                int globalTileId = tileset.firstGid + localIndex;
                int sourceX = (localIndex % tileset.columns) * sourceTileWidth;
                int sourceY = (localIndex / tileset.columns) * sourceTileHeight;
                String imageId = "tile.tmx." + sanitizeId(resourcePath) + "." + globalTileId;
                assets.loadSpriteTile(imageId, normalizeResourcePath(tileset.imageSource), sourceX, sourceY, sourceTileWidth,
                        sourceTileHeight, tileSize, tileSize);
                definitions[globalTileId] = new TileDefinition(globalTileId, imageId, blocked);
            }
        }
        return definitions;
    }

    private List<LayerSpec> parseLayers(Element map, int cols, int rows) {
        List<LayerSpec> layers = new ArrayList<>();
        for (Element layer : childElements(map, "layer")) {
            Element data = firstChild(layer, "data");
            String encoding = stringAttribute(data, "encoding");
            if (!"csv".equalsIgnoreCase(encoding)) {
                throw new IllegalArgumentException("Only CSV-encoded TMX layers are supported");
            }
            layers.add(new LayerSpec(stringAttribute(layer, "name"), parseCsvLayer(data.getTextContent(), cols, rows)));
        }
        return List.copyOf(layers);
    }

    private CollisionProfile buildCollisionProfile(int cols, int rows, List<LayerSpec> layers, List<TilesetSpec> tilesets,
            TileDefinition[] definitions, AssetManager assets) {
        boolean[][] blockedCells = new boolean[cols][rows];
        BufferedImage[][] blockedMasks = new BufferedImage[cols][rows];
        for (LayerSpec layer : layers) {
            boolean plainsSolidLayer = "tile layer 1".equalsIgnoreCase(layer.name());
            boolean overlayLayer = "tile layer 2".equalsIgnoreCase(layer.name());
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    int tileId = layer.tileIds()[col][row];
                    if (tileId < 0) {
                        continue;
                    }
                    TilesetSpec tileset = tilesetFor(tileId, tilesets);
                    if (tileset == null) {
                        continue;
                    }
                    if (overlayLayer && isLargeTreeCanopyTile(tileId, tileset)) {
                        continue;
                    }
                    if (overlayLayer && isLargeTreeTrunkTile(tileId, tileset)) {
                        blockedMasks[col][row] = assets.image(definitions[tileId].imageId());
                        blockedCells[col][row] = true;
                        continue;
                    }
                    if (isSolidTileset(tileset.name) || (plainsSolidLayer && "plains".equalsIgnoreCase(tileset.name))) {
                        blockedCells[col][row] = true;
                    }
                }
            }
        }
        return new CollisionProfile(blockedCells, blockedMasks);
    }

    private BufferedImage[][] buildForegroundMasks(int cols, int rows, List<LayerSpec> layers, List<TilesetSpec> tilesets,
            TileDefinition[] definitions, AssetManager assets) {
        BufferedImage[][] foregroundMasks = new BufferedImage[cols][rows];
        for (LayerSpec layer : layers) {
            boolean overlayLayer = "tile layer 2".equalsIgnoreCase(layer.name());
            if (!overlayLayer) {
                continue;
            }
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    int tileId = layer.tileIds()[col][row];
                    if (tileId < 0) {
                        continue;
                    }
                    TilesetSpec tileset = tilesetFor(tileId, tilesets);
                    if (tileset == null || !"wood".equalsIgnoreCase(tileset.name)) {
                        continue;
                    }
                    BufferedImage source = assets.image(definitions[tileId].imageId());
                    if (isLargeTreeCanopyTile(tileId, tileset)) {
                        foregroundMasks[col][row] = source;
                    } else if (isLargeTreeTrunkTile(tileId, tileset)) {
                        foregroundMasks[col][row] = topMask(source, Math.max(1, source.getHeight() / 2));
                    }
                }
            }
        }
        return foregroundMasks;
    }

    private int[][] parseCsvLayer(String csvText, int cols, int rows) {
        String[] rawValues = csvText.replace("\r", "").replace("\n", ",").split(",");
        List<Integer> values = new ArrayList<>();
        for (String rawValue : rawValues) {
            String trimmed = rawValue.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            values.add(decodeGlobalTileId(trimmed));
        }
        int expectedValues = cols * rows;
        if (values.size() != expectedValues) {
            throw new IllegalArgumentException("Unexpected TMX layer size. Expected " + expectedValues + " values but found " + values.size());
        }

        int[][] tileIds = new int[cols][rows];
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                tileIds[col][row] = values.get(index++);
            }
        }
        return tileIds;
    }

    private int decodeGlobalTileId(String rawValue) {
        long rawId = Long.parseLong(rawValue);
        int globalTileId = (int) (rawId & ~ALL_FLIP_FLAGS);
        return globalTileId == 0 ? -1 : globalTileId;
    }

    private boolean isSolidTileset(String tilesetName) {
        String normalized = tilesetName == null ? "" : tilesetName.trim().toLowerCase();
        return normalized.contains("wood") || normalized.contains("wall") || normalized.contains("fence");
    }

    private TilesetSpec tilesetFor(int globalTileId, List<TilesetSpec> tilesets) {
        for (int i = tilesets.size() - 1; i >= 0; i--) {
            TilesetSpec tileset = tilesets.get(i);
            if (globalTileId >= tileset.firstGid && globalTileId < tileset.firstGid + tileset.tileCount) {
                return tileset;
            }
        }
        return null;
    }

    private boolean isLargeTreeCanopyTile(int globalTileId, TilesetSpec tileset) {
        if (!"wood".equalsIgnoreCase(tileset.name)) {
            return false;
        }
        int localIndex = globalTileId - tileset.firstGid;
        int localCol = localIndex % tileset.columns;
        int localRow = localIndex / tileset.columns;
        return localCol >= 17 && localCol <= 20 && localRow >= 0 && localRow <= 3;
    }

    private boolean isLargeTreeTrunkTile(int globalTileId, TilesetSpec tileset) {
        if (!"wood".equalsIgnoreCase(tileset.name)) {
            return false;
        }
        int localIndex = globalTileId - tileset.firstGid;
        int localCol = localIndex % tileset.columns;
        int localRow = localIndex / tileset.columns;
        return localCol >= 17 && localCol <= 20 && localRow == 4;
    }

    private BufferedImage topMask(BufferedImage source, int visibleHeight) {
        BufferedImage mask = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int clampedHeight = Math.max(0, Math.min(source.getHeight(), visibleHeight));
        for (int y = 0; y < clampedHeight; y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                mask.setRGB(x, y, source.getRGB(x, y));
            }
        }
        return mask;
    }

    private String normalizeResourcePath(String imageSource) {
        if (imageSource == null || imageSource.isBlank()) {
            throw new IllegalArgumentException("TMX tileset image source is missing");
        }
        return imageSource.startsWith("/") ? imageSource : "/" + imageSource;
    }

    private String sanitizeId(String value) {
        return value.replaceAll("[^a-zA-Z0-9]+", ".");
    }

    private List<Element> childElements(Element parent, String tagName) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
                elements.add((Element) child);
            }
        }
        return elements;
    }

    private Element firstChild(Element parent, String tagName) {
        for (Element child : childElements(parent, tagName)) {
            return child;
        }
        throw new IllegalArgumentException("Missing <" + tagName + "> in TMX element <" + parent.getTagName() + ">");
    }

    private int integerAttribute(Element element, String name) {
        return Integer.parseInt(stringAttribute(element, name));
    }

    private String stringAttribute(Element element, String name) {
        String value = element.getAttribute(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing TMX attribute '" + name + "' on <" + element.getTagName() + ">");
        }
        return value;
    }

    private record TilesetSpec(int firstGid, String name, int tileCount, int columns, String imageSource) {
    }

    private record LayerSpec(String name, int[][] tileIds) {
    }

    private record CollisionProfile(boolean[][] blockedCells, BufferedImage[][] blockedMasks) {
    }
}
