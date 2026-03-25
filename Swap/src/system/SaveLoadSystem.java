package system;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import component.InventoryComponent;
import component.PlayerComponent;
import component.PositionComponent;
import ecs.EcsWorld;

public final class SaveLoadSystem {
    public void save(EcsWorld world, Path path) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent pos = world.require(player, PositionComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        Properties properties = new Properties();
        properties.setProperty("player.x", Integer.toString((int) pos.x));
        properties.setProperty("player.y", Integer.toString((int) pos.y));
        properties.setProperty("coins", Integer.toString(inventory.coins));
        properties.setProperty("items", String.join(",", inventory.itemIds));
        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, "swap-rpg reboot save");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar", ex);
        }
    }

    public void load(EcsWorld world, Path path) {
        if (!Files.exists(path)) {
            return;
        }
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar", ex);
        }
        PositionComponent pos = world.require(player, PositionComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        pos.x = Integer.parseInt(properties.getProperty("player.x", Integer.toString((int) pos.x)));
        pos.y = Integer.parseInt(properties.getProperty("player.y", Integer.toString((int) pos.y)));
        inventory.coins = Integer.parseInt(properties.getProperty("coins", "0"));
        inventory.itemIds.clear();
        String items = properties.getProperty("items", "");
        if (!items.isBlank()) {
            for (String item : items.split(",")) {
                inventory.itemIds.add(item);
            }
        }
    }
}
