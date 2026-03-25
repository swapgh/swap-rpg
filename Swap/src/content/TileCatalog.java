package content;

import asset.AssetManager;
import asset.TileDefinition;

/**
 * Traduce el tileset real del proyecto a definiciones consumibles por el mapa.
 *
 * El nombre `TileCatalog` indica que aqui no hay logica de render ni de mundo;
 * solo hay una tabla de referencia que dice: "el tile 40 usa esta imagen y
 * bloquea el paso".
 */
public final class TileCatalog {
    private TileCatalog() {
    }

    /**
     * Construye el array indexado por id de tile.
     *
     * @param assets gestor donde se registran las imagenes de tiles.
     * @param tileSize tamano final al que se escalan los tiles para coincidir con
     * la grilla visual del resto del juego.
     */
    public static TileDefinition[] register(AssetManager assets, int tileSize) {
        TileDefinition[] definitions = new TileDefinition[42];
        load(assets, tileSize, "tile.grass00", "/tiles/grass00.png");
        load(assets, tileSize, "tile.grass01", "/tiles/grass01.png");
        load(assets, tileSize, "tile.water00", "/tiles/water00.png");
        load(assets, tileSize, "tile.water01", "/tiles/water01.png");
        load(assets, tileSize, "tile.water02", "/tiles/water02.png");
        load(assets, tileSize, "tile.water03", "/tiles/water03.png");
        load(assets, tileSize, "tile.water04", "/tiles/water04.png");
        load(assets, tileSize, "tile.water05", "/tiles/water05.png");
        load(assets, tileSize, "tile.water06", "/tiles/water06.png");
        load(assets, tileSize, "tile.water07", "/tiles/water07.png");
        load(assets, tileSize, "tile.water08", "/tiles/water08.png");
        load(assets, tileSize, "tile.water09", "/tiles/water09.png");
        load(assets, tileSize, "tile.water10", "/tiles/water10.png");
        load(assets, tileSize, "tile.water11", "/tiles/water11.png");
        load(assets, tileSize, "tile.water12", "/tiles/water12.png");
        load(assets, tileSize, "tile.water13", "/tiles/water13.png");
        load(assets, tileSize, "tile.road00", "/tiles/road00.png");
        load(assets, tileSize, "tile.road01", "/tiles/road01.png");
        load(assets, tileSize, "tile.road02", "/tiles/road02.png");
        load(assets, tileSize, "tile.road03", "/tiles/road03.png");
        load(assets, tileSize, "tile.road04", "/tiles/road04.png");
        load(assets, tileSize, "tile.road05", "/tiles/road05.png");
        load(assets, tileSize, "tile.road06", "/tiles/road06.png");
        load(assets, tileSize, "tile.road07", "/tiles/road07.png");
        load(assets, tileSize, "tile.road08", "/tiles/road08.png");
        load(assets, tileSize, "tile.road09", "/tiles/road09.png");
        load(assets, tileSize, "tile.road10", "/tiles/road10.png");
        load(assets, tileSize, "tile.road11", "/tiles/road11.png");
        load(assets, tileSize, "tile.road12", "/tiles/road12.png");
        load(assets, tileSize, "tile.earth", "/tiles/earth.png");
        load(assets, tileSize, "tile.wall", "/tiles/wall.png");
        load(assets, tileSize, "tile.tree", "/tiles/tree.png");

        // Los ids 0..10 son placeholders de hierba equivalentes. Se repiten para
        // respetar el formato de mapas heredado sin obligarnos a dibujar 11 PNGs
        // distintos que visualmente hoy representan el mismo suelo.
        for (int i = 0; i <= 10; i++) {
            definitions[i] = new TileDefinition(i, "tile.grass00", false);
        }

        // TileDefinition(id, imageId, blocked):
        // - id: numero que aparece en el archivo del mapa
        // - imageId: clave con la que AssetManager localiza la imagen
        // - blocked: true si ese tile cuenta como colision solida
        definitions[11] = new TileDefinition(11, "tile.grass01", false);
        definitions[12] = new TileDefinition(12, "tile.water00", true);
        definitions[13] = new TileDefinition(13, "tile.water01", true);
        definitions[14] = new TileDefinition(14, "tile.water02", true);
        definitions[15] = new TileDefinition(15, "tile.water03", true);
        definitions[16] = new TileDefinition(16, "tile.water04", true);
        definitions[17] = new TileDefinition(17, "tile.water05", true);
        definitions[18] = new TileDefinition(18, "tile.water06", true);
        definitions[19] = new TileDefinition(19, "tile.water07", true);
        definitions[20] = new TileDefinition(20, "tile.water08", true);
        definitions[21] = new TileDefinition(21, "tile.water09", true);
        definitions[22] = new TileDefinition(22, "tile.water10", true);
        definitions[23] = new TileDefinition(23, "tile.water11", true);
        definitions[24] = new TileDefinition(24, "tile.water12", true);
        definitions[25] = new TileDefinition(25, "tile.water13", true);
        definitions[26] = new TileDefinition(26, "tile.road00", false);
        definitions[27] = new TileDefinition(27, "tile.road01", false);
        definitions[28] = new TileDefinition(28, "tile.road02", false);
        definitions[29] = new TileDefinition(29, "tile.road03", false);
        definitions[30] = new TileDefinition(30, "tile.road04", false);
        definitions[31] = new TileDefinition(31, "tile.road05", false);
        definitions[32] = new TileDefinition(32, "tile.road06", false);
        definitions[33] = new TileDefinition(33, "tile.road07", false);
        definitions[34] = new TileDefinition(34, "tile.road08", false);
        definitions[35] = new TileDefinition(35, "tile.road09", false);
        definitions[36] = new TileDefinition(36, "tile.road10", false);
        definitions[37] = new TileDefinition(37, "tile.road11", false);
        definitions[38] = new TileDefinition(38, "tile.road12", false);
        definitions[39] = new TileDefinition(39, "tile.earth", false);
        definitions[40] = new TileDefinition(40, "tile.wall", true);
        definitions[41] = new TileDefinition(41, "tile.tree", true);
        return definitions;
    }

    private static void load(AssetManager assets, int tileSize, String id, String path) {
        assets.loadImage(id, path, tileSize, tileSize);
    }
}
