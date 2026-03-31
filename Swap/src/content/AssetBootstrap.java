package content;

import asset.AssetManager;

/**
 * Centraliza el registro de recursos graficos del juego.
 *
 * Esta clase existe para que el resto del proyecto no tenga que conocer rutas
 * de archivos reales como "/player/boy_down_1.png". El juego trabaja con ids
 * estables como "player.walk.down" y AssetManager resuelve esos ids a imagenes.
 */
public final class AssetBootstrap {
    private AssetBootstrap() {
    }

    /**
     * Carga y registra todos los recursos base que el juego necesita al arrancar.
     *
     * @param assets gestor central de imagenes, fuentes y clips.
     * @param tileSize tamano visual base del mundo. Se reutiliza para escalar los
     * sprites a la misma grilla que usan mapa, colisiones y render.
     */
    public static void loadAll(AssetManager assets, int tileSize) {
        assets.loadFont("title", "/font/x12y16pxMaruMonica.ttf", 64f);
        assets.loadFont("body", "/font/Purisa Bold.ttf", 28f);
        assets.loadFont("small", "/font/Purisa Bold.ttf", 18f);

        // Estos ids se consumen desde UI y sistemas; por eso se registran aqui y
        // no se dejan repartidos por varias clases.
        assets.loadImage("ui.heartFull", "/objects/heart_full.png", tileSize, tileSize);
        assets.loadImage("ui.heartHalf", "/objects/heart_half.png", tileSize, tileSize);
        assets.loadImage("ui.heartBlank", "/objects/heart_blank.png", tileSize, tileSize);
        assets.loadImage("object.coin", "/objects/coin_bronze.png", tileSize, tileSize);
        assets.loadImage("object.key", "/objects/key.png", tileSize, tileSize);
        assets.loadImage("object.potion", "/objects/potion_red.png", tileSize, tileSize);
        assets.loadImage("object.door", "/objects/door.png", tileSize, tileSize);
        assets.loadImage("object.chest", "/objects/chest.png", tileSize, tileSize);
        assets.loadImage("object.chestOpen", "/objects/chest_opened.png", tileSize, tileSize);

        // Los proyectiles usan media tile para diferenciarse visualmente de una
        // entidad de cuerpo completo sin introducir otra escala arbitraria.
        assets.loadImage("projectile.fire", "/objects/manacrystal_full.png", tileSize / 2, tileSize / 2);
        assets.loadImage("projectile.player", "/objects/blueheart.png", tileSize / 2, tileSize / 2);

        loadPlayer(assets, tileSize);
        loadSlime(assets, tileSize);
        loadOrc(assets, tileSize);
        loadOldMan(assets, tileSize);
        loadMerchant(assets, tileSize);
    }

    /**
     * El jugador tiene clips separados por direccion y por estado.
     *
     * El esquema de nombres `player.walk.down` o `player.attack.left` esta
     * elegido para que AnimationSystem pueda construir ids de forma mecanica a
     * partir de FacingComponent + AnimationSetComponent.
     */
    private static void loadPlayer(AssetManager assets, int tileSize) {
        loadFrame(assets, "player.down.1", "/player/boy_down_1.png", tileSize);
        loadFrame(assets, "player.down.2", "/player/boy_down_2.png", tileSize);
        loadFrame(assets, "player.up.1", "/player/boy_up_1.png", tileSize);
        loadFrame(assets, "player.up.2", "/player/boy_up_2.png", tileSize);
        loadFrame(assets, "player.left.1", "/player/boy_left_1.png", tileSize);
        loadFrame(assets, "player.left.2", "/player/boy_left_2.png", tileSize);
        loadFrame(assets, "player.right.1", "/player/boy_right_1.png", tileSize);
        loadFrame(assets, "player.right.2", "/player/boy_right_2.png", tileSize);
        loadAttackFrame(assets, "player.attack.down.1", "/player/boy_attack_down_1.png", tileSize, tileSize * 2);
        loadAttackFrame(assets, "player.attack.down.2", "/player/boy_attack_down_2.png", tileSize, tileSize * 2);
        loadAttackFrame(assets, "player.attack.up.1", "/player/boy_attack_up_1.png", tileSize, tileSize * 2);
        loadAttackFrame(assets, "player.attack.up.2", "/player/boy_attack_up_2.png", tileSize, tileSize * 2);
        loadAttackFrame(assets, "player.attack.left.1", "/player/boy_attack_left_1.png", tileSize * 2, tileSize);
        loadAttackFrame(assets, "player.attack.left.2", "/player/boy_attack_left_2.png", tileSize * 2, tileSize);
        loadAttackFrame(assets, "player.attack.right.1", "/player/boy_attack_right_1.png", tileSize * 2, tileSize);
        loadAttackFrame(assets, "player.attack.right.2", "/player/boy_attack_right_2.png", tileSize * 2, tileSize);

        assets.registerClip("player.walk.down", "player.down.1", "player.down.2");
        assets.registerClip("player.walk.up", "player.up.1", "player.up.2");
        assets.registerClip("player.walk.left", "player.left.1", "player.left.2");
        assets.registerClip("player.walk.right", "player.right.1", "player.right.2");
        assets.registerClip("player.idle.down", "player.down.1");
        assets.registerClip("player.idle.up", "player.up.1");
        assets.registerClip("player.idle.left", "player.left.1");
        assets.registerClip("player.idle.right", "player.right.1");
        assets.registerClip("player.attack.down", "player.attack.down.1", "player.attack.down.2");
        assets.registerClip("player.attack.up", "player.attack.up.1", "player.attack.up.2");
        assets.registerClip("player.attack.left", "player.attack.left.1", "player.attack.left.2");
        assets.registerClip("player.attack.right", "player.attack.right.1", "player.attack.right.2");
    }

    /**
     * El slime solo tiene arte real mirando hacia abajo.
     *
     * Registramos los cuatro ids igualmente para que el sistema de animacion no
     * necesite un caso especial para este enemigo.
     */
    private static void loadSlime(AssetManager assets, int tileSize) {
        for (String direction : new String[] { "up", "down", "left", "right" }) {
            loadFrame(assets, "enemy.slime." + direction + ".1", "/enemy/greenslime_down_1.png", tileSize);
            loadFrame(assets, "enemy.slime." + direction + ".2", "/enemy/greenslime_down_2.png", tileSize);
            assets.registerClip("enemy.slime.idle." + direction, "enemy.slime." + direction + ".1");
            assets.registerClip("enemy.slime.walk." + direction, "enemy.slime." + direction + ".1",
                    "enemy.slime." + direction + ".2");

            loadFrame(assets, "enemy.redslime." + direction + ".1", "/enemy/redslime_down_1.png", tileSize);
            loadFrame(assets, "enemy.redslime." + direction + ".2", "/enemy/redslime_down_2.png", tileSize);
            assets.registerClip("enemy.redslime.idle." + direction, "enemy.redslime." + direction + ".1");
            assets.registerClip("enemy.redslime.walk." + direction, "enemy.redslime." + direction + ".1",
                    "enemy.redslime." + direction + ".2");
        }
    }

    /**
     * El orco si tiene variantes reales por direccion y por ataque, asi que aqui
     * el loop evita repetir el mismo bloque cuatro veces y mantiene un patron de
     * nombres coherente con el resto del juego.
     */
    private static void loadOrc(AssetManager assets, int tileSize) {
        for (String direction : new String[] { "up", "down", "left", "right" }) {
            loadFrame(assets, "enemy.orc." + direction + ".1", "/enemy/orc_" + direction + "_1.png", tileSize);
            loadFrame(assets, "enemy.orc." + direction + ".2", "/enemy/orc_" + direction + "_2.png", tileSize);
            loadFrame(assets, "enemy.orc.attack." + direction + ".1", "/enemy/orc_attack_" + direction + "_1.png", tileSize);
            loadFrame(assets, "enemy.orc.attack." + direction + ".2", "/enemy/orc_attack_" + direction + "_2.png", tileSize);
            assets.registerClip("enemy.orc.idle." + direction, "enemy.orc." + direction + ".1");
            assets.registerClip("enemy.orc.walk." + direction, "enemy.orc." + direction + ".1", "enemy.orc." + direction + ".2");
            assets.registerClip("enemy.orc.attack." + direction, "enemy.orc.attack." + direction + ".1", "enemy.orc.attack." + direction + ".2");
        }
    }

    private static void loadOldMan(AssetManager assets, int tileSize) {
        for (String direction : new String[] { "up", "down", "left", "right" }) {
            loadFrame(assets, "npc.oldman." + direction + ".1", "/npc/oldman_" + direction + "_1.png", tileSize);
            loadFrame(assets, "npc.oldman." + direction + ".2", "/npc/oldman_" + direction + "_2.png", tileSize);
            assets.registerClip("npc.oldman.idle." + direction, "npc.oldman." + direction + ".1");
            assets.registerClip("npc.oldman.walk." + direction, "npc.oldman." + direction + ".1", "npc.oldman." + direction + ".2");
        }
    }

    /**
     * El merchant hoy solo tiene frames mirando hacia abajo.
     *
     * Igual que con el slime, duplicamos esos frames bajo ids por direccion para
     * mantener uniforme el contrato entre contenido y AnimationSystem.
     */
    private static void loadMerchant(AssetManager assets, int tileSize) {
        for (String direction : new String[] { "up", "down", "left", "right" }) {
            loadFrame(assets, "npc.merchant." + direction + ".1", "/npc/merchant_down_1.png", tileSize);
            loadFrame(assets, "npc.merchant." + direction + ".2", "/npc/merchant_down_2.png", tileSize);
            assets.registerClip("npc.merchant.idle." + direction, "npc.merchant." + direction + ".1");
            assets.registerClip("npc.merchant.walk." + direction, "npc.merchant." + direction + ".1",
                    "npc.merchant." + direction + ".2");
        }
    }

    /**
     * `id` es el nombre interno con el que el resto del juego pedira la imagen.
     * `path` es la ruta real en /res.
     */
    private static void loadFrame(AssetManager assets, String id, String path, int tileSize) {
        assets.loadImage(id, path, tileSize, tileSize);
    }

    private static void loadAttackFrame(AssetManager assets, String id, String path, int width, int height) {
        assets.loadImage(id, path, width, height);
    }
}
