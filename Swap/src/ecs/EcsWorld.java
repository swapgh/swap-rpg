package ecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * EcsWorld es el contenedor central del modelo ECS.
 *
 * Aqui no vive la logica del juego; solo vive el almacenamiento de entidades y
 * componentes. En este proyecto una entidad es un int y los componentes son
 * objetos de datos puros guardados por tipo.
 */
public final class EcsWorld {
    /**
     * Generador monotono de ids.
     *
     * Empezamos en 1 para que el 0 no tenga significado accidental si algun dia
     * se usa como valor por defecto o "no asignado".
     */
    private int nextEntityId = 1;

    /**
     * Entidades actualmente vivas.
     *
     * LinkedHashSet mantiene orden estable de insercion; eso ayuda a depurar y
     * hace que ciertos recorridos sean mas predecibles entre ejecuciones.
     */
    private final Set<Integer> alive = new LinkedHashSet<>();

    /**
     * Almacenamiento por tipo de componente.
     *
     * La clave externa es la clase del componente, por ejemplo PositionComponent.
     * La clave interna es el id de entidad. Esto hace muy directo preguntar
     * "la entidad X tiene este componente?" sin construir objetos Entity pesados.
     */
    private final Map<Class<?>, Map<Integer, Object>> stores = new HashMap<>();

    /**
     * Crea una entidad vacia.
     *
     * En ECS la identidad nace primero y despues se le van montando componentes.
     */
    public int createEntity() {
        int entity = nextEntityId++;
        alive.add(entity);
        return entity;
    }

    /**
     * Elimina una entidad y todos sus componentes.
     *
     * Borrar la entidad implica limpiar cada store para no dejar datos huerfanos.
     */
    public void destroyEntity(int entity) {
        alive.remove(entity);
        for (Map<Integer, Object> store : stores.values()) {
            store.remove(entity);
        }
    }

    public boolean isAlive(int entity) {
        return alive.contains(entity);
    }

    /**
     * Devuelve una vista inmutable de las entidades vivas.
     */
    public Collection<Integer> entities() {
        return List.copyOf(alive);
    }

    /**
     * Asocia un componente a una entidad.
     *
     * Usamos component.getClass() porque en esta base cada entidad guarda una
     * sola instancia por tipo concreto de componente.
     */
    public <T> void add(int entity, T component) {
        stores.computeIfAbsent(component.getClass(), key -> new HashMap<>()).put(entity, component);
    }

    /**
     * Devuelve el componente si existe, o null si la entidad no lo tiene.
     */
    public <T> T get(int entity, Class<T> type) {
        Map<Integer, Object> store = stores.get(type);
        if (store == null) {
            return null;
        }
        return type.cast(store.get(entity));
    }

    /**
     * Variante estricta de get.
     *
     * Se usa cuando la ausencia del componente seria un error de programacion y
     * queremos fallar pronto con un mensaje claro.
     */
    public <T> T require(int entity, Class<T> type) {
        T component = get(entity, type);
        if (component == null) {
            throw new IllegalStateException("Entity " + entity + " missing component " + type.getSimpleName());
        }
        return component;
    }

    public boolean has(int entity, Class<?> type) {
        Map<Integer, Object> store = stores.get(type);
        return store != null && store.containsKey(entity);
    }

    /**
     * Quita un componente concreto sin destruir la entidad completa.
     *
     * Esto es util cuando una entidad cambia de estado por composicion, por
     * ejemplo un cofre que deja de ser coleccionable despues de abrirse.
     */
    public <T> void remove(int entity, Class<T> type) {
        Map<Integer, Object> store = stores.get(type);
        if (store != null) {
            store.remove(entity);
        }
    }

    /**
     * Consulta sencilla por "tiene todos estos componentes".
     *
     * No es el query system mas sofisticado del mundo, pero para este proyecto
     * mantiene el nucleo ECS muy pequeño y facil de entender.
     */
    @SafeVarargs
    public final List<Integer> entitiesWith(Class<?>... types) {
        List<Integer> matches = new ArrayList<>();
        outer: for (int entity : alive) {
            for (Class<?> type : types) {
                if (!has(entity, type)) {
                    continue outer;
                }
            }
            matches.add(entity);
        }
        return matches;
    }
}
