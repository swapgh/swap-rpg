package ui.text;

public final class UiText {
    public static final String GAME_TITLE = "Swap RPG";
    public static final String START_PROMPT = "Pulsa ENTER para empezar";
    public static final String MENU_ACCESS = "Acceso";
    public static final String MENU_MAIN = "Menu principal";
    public static final String MENU_MAIN_MENU = "Menu principal";
    public static final String MENU_CONTINUE = "Continuar";
    public static final String MENU_LOAD_SAVE = "Cargar guardado";
    public static final String MENU_SAVE_GAME = "Quick save";
    public static final String MENU_SAVE_AS = "Guardar como";
    public static final String MENU_RENAME_SAVE = "Renombrar";
    public static final String MENU_DELETE_SAVE = "Borrar";
    public static final String MENU_OPTIONS = "Opciones";
    public static final String MENU_KEYBINDS = "Controles";
    public static final String MENU_NEW_GAME = "Nueva partida";
    public static final String MENU_BACK = "Volver";
    public static final String MENU_SWITCH_ACCOUNT = "Cambiar cuenta";
    public static final String MENU_BACK_TO_ACCESS = "Volver a acceso";
    public static final String MENU_LOGOUT = "Logout";
    public static final String MENU_LOGIN = "Iniciar sesion";
    public static final String MENU_REGISTER = "Registrarse";
    public static final String MENU_GUEST = "Continuar como invitado";
    public static final String MENU_EXIT = "Salir";
    public static final String MENU_CLOSE_APP = "Cerrar app";
    public static final String FOOTER_SELECT = "ENTER elegir";
    public static final String FOOTER_PLAY = "ENTER jugar";
    public static final String FOOTER_NAVIGATION = "W/S mover  ENTER elegir";
    public static final String ACCOUNT = "Cuenta";
    public static final String GUEST = "Invitado";
    public static final String LOGIN_AS_GUEST = "Entraste como invitado.";
    public static final String LOGIN_LOG = "Log";
    public static final String LOGIN_OPEN_LOG = "L abrir";
    public static final String LOGIN_CLOSE_LOG = "L cerrar";
    public static final String INVENTORY = "Inventario";
    public static final String INVENTORY_CLOSE = "I cerrar";
    public static final String INVENTORY_EMPTY_SELECTION = "Sin seleccion";
    public static final String SHOP = "Tienda";
    public static final String SHOP_CLOSE = "ESC cerrar";
    public static final String SHOP_BUY = "ENTER comprar";
    public static final String WORLD_HINT_CONTINUE = "E continuar";
    public static final String WORLD_HINT_TALK = "E hablar";
    public static final String WORLD_HINT_OPEN_DOOR = "E abrir puerta";
    public static final String WORLD_HINT_OPEN_CHEST = "E abrir cofre";
    public static final String WORLD_HINT_OPEN_SHOP = "E comerciar";
    public static final String STATUS_NO_SAVE = "No hay partida guardada.";
    public static final String STATUS_NO_MANUAL_SAVE = "No hay guardado manual.";
    public static final String STATUS_AUTOSAVED = "Autosave actualizado.";
    public static final String STATUS_MANUAL_SAVED = "Partida guardada.";
    public static final String STATUS_MANUAL_SAVE_CANCELLED = "Guardado manual cancelado.";
    public static final String STATUS_QUICKSAVE_DONE = "Quick save actualizado.";
    public static final String STATUS_SAVE_RENAMED = "Guardado renombrado.";
    public static final String STATUS_SAVE_DELETED = "Guardado borrado.";
    public static final String STATUS_LAST_SAVE_MISSING = "El ultimo save usado ya no existe.";
    public static final String STATUS_AUTOSAVE_MISSING = "No hay autosave disponible.";
    public static final String STATUS_RESET_FAILED = "No se pudo reiniciar la partida.";
    public static final String GAME_OVER_TITLE = "Game Over";
    public static final String SECTION_SAVE_SLOTS = "Guardados";
    public static final String SECTION_SAVE_ACTIONS = "Acciones";
    public static final String SECTION_KEYBINDS = "Controles";
    public static final String STATUS_DAMAGE_SUFFIX = " vida";
    public static final String STATUS_PROJECTILE_SUFFIX = " proyectil";
    public static final String STATUS_HIT = "Impacto";
    public static final String STATUS_DOOR_OPENED = "Puerta abierta";
    public static final String STATUS_MISSING_KEY = "Falta llave";
    public static final String STATUS_CHEST_OPENED = "Cofre abierto";
    public static final String STATUS_QUEST_FIRST_COIN = "Quest completada: primera moneda";
    public static final String STATUS_QUEST_FIRST_KILL = "Quest completada: primera victoria";
    public static final String STATUS_QUEST_DAY_VISIT_COMPLETE = "Quest completada: visita al mercader";
    public static final String STATUS_QUEST_NIGHT_VISIT_COMPLETE = "Quest completada: guardia nocturna";
    public static final String STATUS_DAY_BREAK = "Amanece";
    public static final String STATUS_NIGHT_FALL = "Anochece";
    public static final String STATUS_SHOP_NO_STOCK = "Sin stock disponible.";
    public static final String STATUS_SHOP_NOT_ENOUGH_COINS = "No tienes monedas suficientes.";
    public static final String STATUS_SHOP_PURCHASED = "Compra realizada.";
    public static final String LABEL_COINS = "Monedas";
    public static final String LABEL_BAG = "Bolsa";
    public static final String LABEL_OCCUPIED = "Ocupado";
    public static final String LABEL_DAY = "Dia";
    public static final String LABEL_NIGHT = "Noche";

    private UiText() {
    }

    public static String accountLabel(boolean loggedIn) {
        return loggedIn ? ACCOUNT : GUEST;
    }

    public static String menuAccountOption(boolean loggedIn) {
        return loggedIn ? MENU_LOGOUT : MENU_BACK_TO_ACCESS;
    }

    public static String footerForSave(boolean hasSave) {
        return hasSave ? FOOTER_PLAY : FOOTER_SELECT;
    }

    public static String saveCreated(String saveName) {
        return "Guardado: " + saveName;
    }

    public static String quickSaveCreated(String saveName) {
        return "Quick save: " + saveName;
    }

    public static String inventoryOccupied(int used, int capacity) {
        return LABEL_OCCUPIED + ": " + used + "/" + capacity;
    }

    public static String inventoryCoins(int coins) {
        return LABEL_COINS + ": " + coins;
    }

    public static String hudInventoryStats(int coins, int occupied, int capacity) {
        return LABEL_COINS + " " + coins + "   " + LABEL_BAG + " " + occupied + "/" + capacity;
    }

    public static String worldTimeLabel(int day, int hour, int minute, int second, boolean dayPhase) {
        return String.format("%s %d  %02d:%02d:%02d", dayPhase ? LABEL_DAY : LABEL_NIGHT, day, hour, minute, second);
    }

    public static String itemCount(int count) {
        return "x" + count;
    }

    public static String selectedItemLabel(String displayName, int count) {
        return displayName + " " + itemCount(count);
    }

    public static String playerDamage(int damage) {
        return "-" + damage + STATUS_DAMAGE_SUFFIX;
    }

    public static String projectileDamage(int damage) {
        return "-" + damage + STATUS_PROJECTILE_SUFFIX;
    }

    public static String enemyDamage(String enemyName, int damage) {
        return enemyName + " -" + damage;
    }

    public static String itemPickedUp(String itemId) {
        return "Recogiste " + itemId;
    }

    public static String shopCoins(int coins) {
        return LABEL_COINS + ": " + coins;
    }

    public static String shopEntry(String itemName, int price, int stock) {
        String stockLabel = stock < 0 ? "inf" : Integer.toString(stock);
        return itemName + "  $" + price + "  stk " + stockLabel;
    }
}
