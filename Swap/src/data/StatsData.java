package data;

/**
 * Core combat and movement numbers shared by multiple entity types.
 */
public record StatsData(
        int health,
        int speed,
        int attack,
        int defense) {
}
