package ecs;

/**
 * Un sistema ECS contiene comportamiento, no datos.
 *
 * La idea es que cada implementacion recorre el mundo y procesa solo las
 * entidades que tengan el conjunto de componentes que necesita.
 */
public interface EcsSystem {
    /**
     * @param world mundo ECS compartido; contiene entidades y componentes.
     * @param dtSeconds delta time del frame en segundos. Aunque varios systems
     * usen ticks fijos hoy, dejamos este parametro para no cerrar la puerta a
     * comportamiento dependiente del tiempo mas adelante.
     */
    void update(EcsWorld world, double dtSeconds);
}
