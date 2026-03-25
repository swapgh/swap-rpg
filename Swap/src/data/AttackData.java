package data;

/**
 * Describes close-range combat values for an entity type.
 *
 * `rangeScale` is expressed relative to the runtime tile size so the same data file
 * stays valid if the project later changes its global visual scale.
 */
public record AttackData(
        int damage,
        double rangeScale,
        int cooldownTicks) {
}
