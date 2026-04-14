package asset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import util.ResourceStreams;

public final class MapLoader {
    public TileMap load(String resourcePath, int tileSize, TileDefinition[] definitions) {
        return load(List.of(resourcePath), tileSize, definitions);
    }

    public TileMap load(List<String> resourcePaths, int tileSize, TileDefinition[] definitions) {
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            throw new IllegalArgumentException("At least one map layer resource is required");
        }
        List<int[][]> layers = new ArrayList<>();
        int cols = -1;
        int rows = -1;
        for (String resourcePath : resourcePaths) {
            int[][] layer = loadLayer(resourcePath);
            if (cols < 0) {
                cols = layer.length;
                rows = layer[0].length;
            } else if (cols != layer.length || rows != layer[0].length) {
                throw new IllegalArgumentException("Map layer dimensions do not match: " + resourcePath);
            }
            layers.add(layer);
        }
        int[][][] layerTileIds = new int[layers.size()][][];
        for (int i = 0; i < layers.size(); i++) {
            layerTileIds[i] = layers.get(i);
        }
        return new TileMap(tileSize, cols, rows, layerTileIds, definitions);
    }

    private int[][] loadLayer(String resourcePath) {
        List<int[]> rows = new ArrayList<>();
        boolean tiledCsv = resourcePath.toLowerCase().endsWith(".csv");
        try (InputStream is = ResourceStreams.open(MapLoader.class, resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing map resource: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    String[] numbers = splitRow(trimmed);
                    int[] row = new int[numbers.length];
                    for (int i = 0; i < numbers.length; i++) {
                        row[i] = parseTileId(numbers[i], resourcePath, tiledCsv);
                    }
                    rows.add(row);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load map " + resourcePath, ex);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Map resource has no rows: " + resourcePath);
        }
        int cols = rows.get(0).length;
        int[][] tileIds = new int[cols][rows.size()];
        for (int row = 0; row < rows.size(); row++) {
            int[] source = rows.get(row);
            if (source.length != cols) {
                throw new IllegalArgumentException("Inconsistent map row width in " + resourcePath);
            }
            for (int col = 0; col < cols; col++) {
                tileIds[col][row] = source[col];
            }
        }
        return tileIds;
    }

    private String[] splitRow(String row) {
        if (row.indexOf(',') >= 0) {
            return row.split("\\s*,\\s*");
        }
        return row.split("\\s+");
    }

    private int parseTileId(String rawValue, String resourcePath, boolean tiledCsv) {
        try {
            int value = Integer.parseInt(rawValue.trim());
            if (tiledCsv && value > 0) {
                return value - 1;
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid tile id '" + rawValue + "' in map " + resourcePath, ex);
        }
    }
}
