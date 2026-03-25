package asset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class MapLoader {
    public TileMap load(String resourcePath, int tileSize, TileDefinition[] definitions) {
        List<int[]> rows = new ArrayList<>();
        try (InputStream is = MapLoader.class.getResourceAsStream(resourcePath)) {
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
                    String[] numbers = trimmed.split("\\s+");
                    int[] row = new int[numbers.length];
                    for (int i = 0; i < numbers.length; i++) {
                        row[i] = Integer.parseInt(numbers[i]);
                    }
                    rows.add(row);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load map " + resourcePath, ex);
        }
        int cols = rows.get(0).length;
        int[][] tileIds = new int[cols][rows.size()];
        for (int row = 0; row < rows.size(); row++) {
            int[] source = rows.get(row);
            for (int col = 0; col < cols; col++) {
                tileIds[col][row] = source[col];
            }
        }
        return new TileMap(tileSize, cols, rows.size(), tileIds, definitions);
    }
}
