package ui.text;

import data.JsonDataLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import util.ResourceStreams;

public final class UiText {
    public static String GAME_TITLE;
    public static String START_PROMPT;
    public static String MENU_ACCESS;
    public static String MENU_MAIN;
    public static String MENU_MAIN_MENU;
    public static String MENU_CONTINUE;
    public static String MENU_LOAD_SAVE;
    public static String MENU_SAVE_GAME;
    public static String MENU_SAVE_AS;
    public static String MENU_RENAME_SAVE;
    public static String MENU_DELETE_SAVE;
    public static String MENU_OPTIONS;
    public static String MENU_KEYBINDS;
    public static String MENU_NEW_GAME;
    public static String MENU_BACK;
    public static String MENU_SWITCH_ACCOUNT;
    public static String MENU_BACK_TO_ACCESS;
    public static String MENU_LOGOUT;
    public static String MENU_LOGIN;
    public static String MENU_REGISTER;
    public static String MENU_GUEST;
    public static String MENU_EXIT;
    public static String MENU_CLOSE_APP;
    public static String MENU_LANGUAGE;
    public static String FOOTER_SELECT;
    public static String FOOTER_PLAY;
    public static String FOOTER_NAVIGATION;
    public static String FOOTER_ENTER_OPEN_BACK;
    public static String FOOTER_ENTER_CHOOSE_BACK;
    public static String ACCOUNT;
    public static String GUEST;
    public static String LOGIN_AS_GUEST;
    public static String LOGIN_LOG;
    public static String LOGIN_OPEN_LOG;
    public static String LOGIN_CLOSE_LOG;
    public static String LOGIN_CANCELLED;
    public static String LOGIN_SUCCESS;
    public static String REGISTER_CANCELLED;
    public static String REGISTER_SUCCESS;
    public static String DIALOG_SWAP_WEB_URL;
    public static String DIALOG_EMAIL;
    public static String DIALOG_PASSWORD;
    public static String DIALOG_USERNAME;
    public static String DIALOG_USERNAME_OR_EMAIL;
    public static String DIALOG_REMEMBER_SESSION;
    public static String DIALOG_LOGIN_TITLE;
    public static String DIALOG_REGISTER_TITLE;
    public static String DIALOG_SAVE_NAME;
    public static String DIALOG_SAVE_GAME;
    public static String DIALOG_DELETE_SAVE;
    public static String INVENTORY;
    public static String INVENTORY_CLOSE;
    public static String INVENTORY_EMPTY_SELECTION;
    public static String CHARACTER;
    public static String CHARACTER_CLOSE;
    public static String CHARACTER_ATTRIBUTES;
    public static String CHARACTER_STATS;
    public static String CHARACTER_SLOT_WEAPON;
    public static String CHARACTER_SLOT_OFFHAND;
    public static String CHARACTER_SLOT_ARMOR;
    public static String CHARACTER_SLOT_BOOTS;
    public static String SHOP;
    public static String SHOP_CLOSE;
    public static String SHOP_BUY;
    public static String SHOP_KEEPER;
    public static String LOOT;
    public static String LOOT_CLOSE;
    public static String LOOT_TAKE;
    public static String LOOT_CONTAINER;
    public static String STATUS_LOOT_EMPTY;
    public static String STATUS_LOOT_TAKEN;
    public static String WORLD_HINT_CONTINUE;
    public static String WORLD_HINT_TALK;
    public static String WORLD_HINT_OPEN_DOOR;
    public static String WORLD_HINT_OPEN_CHEST;
    public static String WORLD_HINT_OPEN_SHOP;
    public static String STATUS_NO_SAVE;
    public static String STATUS_NO_MANUAL_SAVE;
    public static String STATUS_AUTOSAVED;
    public static String STATUS_MANUAL_SAVED;
    public static String STATUS_MANUAL_SAVE_CANCELLED;
    public static String STATUS_QUICKSAVE_DONE;
    public static String STATUS_SAVE_RENAMED;
    public static String STATUS_SAVE_DELETED;
    public static String STATUS_LAST_SAVE_MISSING;
    public static String STATUS_AUTOSAVE_MISSING;
    public static String STATUS_RESET_FAILED;
    public static String GAME_OVER_TITLE;
    public static String SECTION_SAVE_SLOTS;
    public static String SECTION_SAVE_ACTIONS;
    public static String SECTION_KEYBINDS;
    public static String STATUS_DAMAGE_SUFFIX;
    public static String STATUS_PROJECTILE_SUFFIX;
    public static String STATUS_HIT;
    public static String STATUS_DOOR_OPENED;
    public static String STATUS_MISSING_KEY;
    public static String STATUS_CHEST_OPENED;
    public static String STATUS_QUEST_FIRST_COIN;
    public static String STATUS_QUEST_FIRST_KILL;
    public static String STATUS_QUEST_DAY_VISIT_COMPLETE;
    public static String STATUS_QUEST_NIGHT_VISIT_COMPLETE;
    public static String STATUS_DAY_BREAK;
    public static String STATUS_NIGHT_FALL;
    public static String STATUS_SHOP_NO_STOCK;
    public static String STATUS_SHOP_NOT_ENOUGH_COINS;
    public static String STATUS_SHOP_PURCHASED;
    public static String LABEL_COINS;
    public static String LABEL_BAG;
    public static String LABEL_OCCUPIED;
    public static String LABEL_DAY;
    public static String LABEL_NIGHT;
    public static String LABEL_HP;
    public static String LABEL_MANA;
    public static String LABEL_LEVEL;
    public static String LABEL_CLASS;
    public static String LABEL_AUTOSAVE;
    public static String LABEL_SAVE_FALLBACK;
    public static String LABEL_LANGUAGE_EN;
    public static String LABEL_LANGUAGE_ES;
    public static String KEYBIND_MOVE;
    public static String KEYBIND_INTERACT;
    public static String KEYBIND_MELEE;
    public static String KEYBIND_PROJECTILE;
    public static String KEYBIND_INVENTORY;
    public static String KEYBIND_CHARACTER;
    public static String KEYBIND_QUICKSAVE;
    public static String KEYBIND_MANUAL_SAVE;
    public static String KEYBIND_OPTIONS;
    public static String KEYBIND_DAY_NIGHT;

    private static UiLanguage currentLanguage = UiLanguage.EN;

    static {
        applyLanguage(UiLanguage.EN);
    }

    private UiText() {
    }

    public static UiLanguage language() {
        return currentLanguage;
    }

    public static void toggleLanguage() {
        applyLanguage(currentLanguage.next());
    }

    public static void applyLanguage(UiLanguage language) {
        Map<String, Object> map = loadBundle(language.resourcePath());
        currentLanguage = language;
        GAME_TITLE = text(map, "gameTitle");
        START_PROMPT = text(map, "startPrompt");
        MENU_ACCESS = text(map, "menuAccess");
        MENU_MAIN = text(map, "menuMain");
        MENU_MAIN_MENU = text(map, "menuMainMenu");
        MENU_CONTINUE = text(map, "menuContinue");
        MENU_LOAD_SAVE = text(map, "menuLoadSave");
        MENU_SAVE_GAME = text(map, "menuSaveGame");
        MENU_SAVE_AS = text(map, "menuSaveAs");
        MENU_RENAME_SAVE = text(map, "menuRenameSave");
        MENU_DELETE_SAVE = text(map, "menuDeleteSave");
        MENU_OPTIONS = text(map, "menuOptions");
        MENU_KEYBINDS = text(map, "menuKeybinds");
        MENU_NEW_GAME = text(map, "menuNewGame");
        MENU_BACK = text(map, "menuBack");
        MENU_SWITCH_ACCOUNT = text(map, "menuSwitchAccount");
        MENU_BACK_TO_ACCESS = text(map, "menuBackToAccess");
        MENU_LOGOUT = text(map, "menuLogout");
        MENU_LOGIN = text(map, "menuLogin");
        MENU_REGISTER = text(map, "menuRegister");
        MENU_GUEST = text(map, "menuGuest");
        MENU_EXIT = text(map, "menuExit");
        MENU_CLOSE_APP = text(map, "menuCloseApp");
        MENU_LANGUAGE = text(map, "menuLanguage");
        FOOTER_SELECT = text(map, "footerSelect");
        FOOTER_PLAY = text(map, "footerPlay");
        FOOTER_NAVIGATION = text(map, "footerNavigation");
        FOOTER_ENTER_OPEN_BACK = text(map, "footerEnterOpenBack");
        FOOTER_ENTER_CHOOSE_BACK = text(map, "footerEnterChooseBack");
        ACCOUNT = text(map, "account");
        GUEST = text(map, "guest");
        LOGIN_AS_GUEST = text(map, "loginAsGuest");
        LOGIN_LOG = text(map, "loginLog");
        LOGIN_OPEN_LOG = text(map, "loginOpenLog");
        LOGIN_CLOSE_LOG = text(map, "loginCloseLog");
        LOGIN_CANCELLED = text(map, "loginCancelled");
        LOGIN_SUCCESS = text(map, "loginSuccess");
        REGISTER_CANCELLED = text(map, "registerCancelled");
        REGISTER_SUCCESS = text(map, "registerSuccess");
        DIALOG_SWAP_WEB_URL = text(map, "dialogSwapWebUrl");
        DIALOG_EMAIL = text(map, "dialogEmail");
        DIALOG_PASSWORD = text(map, "dialogPassword");
        DIALOG_USERNAME = text(map, "dialogUsername");
        DIALOG_USERNAME_OR_EMAIL = text(map, "dialogUsernameOrEmail");
        DIALOG_REMEMBER_SESSION = text(map, "dialogRememberSession");
        DIALOG_LOGIN_TITLE = text(map, "dialogLoginTitle");
        DIALOG_REGISTER_TITLE = text(map, "dialogRegisterTitle");
        DIALOG_SAVE_NAME = text(map, "dialogSaveName");
        DIALOG_SAVE_GAME = text(map, "dialogSaveGame");
        DIALOG_DELETE_SAVE = text(map, "dialogDeleteSave");
        INVENTORY = text(map, "inventory");
        INVENTORY_CLOSE = text(map, "inventoryClose");
        INVENTORY_EMPTY_SELECTION = text(map, "inventoryEmptySelection");
        CHARACTER = text(map, "character");
        CHARACTER_CLOSE = text(map, "characterClose");
        CHARACTER_ATTRIBUTES = text(map, "characterAttributes");
        CHARACTER_STATS = text(map, "characterStats");
        CHARACTER_SLOT_WEAPON = text(map, "characterSlotWeapon");
        CHARACTER_SLOT_OFFHAND = text(map, "characterSlotOffhand");
        CHARACTER_SLOT_ARMOR = text(map, "characterSlotArmor");
        CHARACTER_SLOT_BOOTS = text(map, "characterSlotBoots");
        SHOP = text(map, "shop");
        SHOP_CLOSE = text(map, "shopClose");
        SHOP_BUY = text(map, "shopBuy");
        SHOP_KEEPER = text(map, "shopKeeper");
        LOOT = text(map, "loot");
        LOOT_CLOSE = text(map, "lootClose");
        LOOT_TAKE = text(map, "lootTake");
        LOOT_CONTAINER = text(map, "lootContainer");
        STATUS_LOOT_EMPTY = text(map, "statusLootEmpty");
        STATUS_LOOT_TAKEN = text(map, "statusLootTaken");
        WORLD_HINT_CONTINUE = text(map, "worldHintContinue");
        WORLD_HINT_TALK = text(map, "worldHintTalk");
        WORLD_HINT_OPEN_DOOR = text(map, "worldHintOpenDoor");
        WORLD_HINT_OPEN_CHEST = text(map, "worldHintOpenChest");
        WORLD_HINT_OPEN_SHOP = text(map, "worldHintOpenShop");
        STATUS_NO_SAVE = text(map, "statusNoSave");
        STATUS_NO_MANUAL_SAVE = text(map, "statusNoManualSave");
        STATUS_AUTOSAVED = text(map, "statusAutosaved");
        STATUS_MANUAL_SAVED = text(map, "statusManualSaved");
        STATUS_MANUAL_SAVE_CANCELLED = text(map, "statusManualSaveCancelled");
        STATUS_QUICKSAVE_DONE = text(map, "statusQuicksaveDone");
        STATUS_SAVE_RENAMED = text(map, "statusSaveRenamed");
        STATUS_SAVE_DELETED = text(map, "statusSaveDeleted");
        STATUS_LAST_SAVE_MISSING = text(map, "statusLastSaveMissing");
        STATUS_AUTOSAVE_MISSING = text(map, "statusAutosaveMissing");
        STATUS_RESET_FAILED = text(map, "statusResetFailed");
        GAME_OVER_TITLE = text(map, "gameOverTitle");
        SECTION_SAVE_SLOTS = text(map, "sectionSaveSlots");
        SECTION_SAVE_ACTIONS = text(map, "sectionSaveActions");
        SECTION_KEYBINDS = text(map, "sectionKeybinds");
        STATUS_DAMAGE_SUFFIX = text(map, "statusDamageSuffix");
        STATUS_PROJECTILE_SUFFIX = text(map, "statusProjectileSuffix");
        STATUS_HIT = text(map, "statusHit");
        STATUS_DOOR_OPENED = text(map, "statusDoorOpened");
        STATUS_MISSING_KEY = text(map, "statusMissingKey");
        STATUS_CHEST_OPENED = text(map, "statusChestOpened");
        STATUS_QUEST_FIRST_COIN = text(map, "statusQuestFirstCoin");
        STATUS_QUEST_FIRST_KILL = text(map, "statusQuestFirstKill");
        STATUS_QUEST_DAY_VISIT_COMPLETE = text(map, "statusQuestDayVisitComplete");
        STATUS_QUEST_NIGHT_VISIT_COMPLETE = text(map, "statusQuestNightVisitComplete");
        STATUS_DAY_BREAK = text(map, "statusDayBreak");
        STATUS_NIGHT_FALL = text(map, "statusNightFall");
        STATUS_SHOP_NO_STOCK = text(map, "statusShopNoStock");
        STATUS_SHOP_NOT_ENOUGH_COINS = text(map, "statusShopNotEnoughCoins");
        STATUS_SHOP_PURCHASED = text(map, "statusShopPurchased");
        LABEL_COINS = text(map, "labelCoins");
        LABEL_BAG = text(map, "labelBag");
        LABEL_OCCUPIED = text(map, "labelOccupied");
        LABEL_DAY = text(map, "labelDay");
        LABEL_NIGHT = text(map, "labelNight");
        LABEL_HP = text(map, "labelHp");
        LABEL_MANA = text(map, "labelMana");
        LABEL_LEVEL = text(map, "labelLevel");
        LABEL_CLASS = text(map, "labelClass");
        LABEL_AUTOSAVE = text(map, "labelAutosave");
        LABEL_SAVE_FALLBACK = text(map, "labelSaveFallback");
        LABEL_LANGUAGE_EN = text(map, "labelLanguageEn");
        LABEL_LANGUAGE_ES = text(map, "labelLanguageEs");
        KEYBIND_MOVE = text(map, "keybindMove");
        KEYBIND_INTERACT = text(map, "keybindInteract");
        KEYBIND_MELEE = text(map, "keybindMelee");
        KEYBIND_PROJECTILE = text(map, "keybindProjectile");
        KEYBIND_INVENTORY = text(map, "keybindInventory");
        KEYBIND_CHARACTER = text(map, "keybindCharacter");
        KEYBIND_QUICKSAVE = text(map, "keybindQuicksave");
        KEYBIND_MANUAL_SAVE = text(map, "keybindManualSave");
        KEYBIND_OPTIONS = text(map, "keybindOptions");
        KEYBIND_DAY_NIGHT = text(map, "keybindDayNight");
        ContentText.reload(language);
    }

    public static String accountLabel(boolean loggedIn) {
        return loggedIn ? format("Connected", "Conectado") : GUEST;
    }

    public static String menuAccountOption(boolean loggedIn) {
        return loggedIn ? MENU_LOGOUT : MENU_BACK_TO_ACCESS;
    }

    public static String footerForSave(boolean hasSave) {
        return hasSave ? FOOTER_PLAY : FOOTER_SELECT;
    }

    public static String saveCreated(String saveName) {
        return format("Saved: %s", "Guardado: %s", saveName);
    }

    public static String quickSaveCreated(String saveName) {
        return format("Quick save: %s", "Quick save: %s", saveName);
    }

    public static String rosterSyncSummary(int found, int synced, int failed) {
        return format("Roster sync %d/%d (%d failed)", "Sync roster %d/%d (%d fallaron)", synced, found, failed);
    }

    public static String classLabel(String classId) {
        return switch (classId == null ? "" : classId.trim().toLowerCase()) {
        case "mage" -> format("Mage", "Mago");
        case "druid" -> format("Druid", "Druida");
        default -> format("Warrior", "Guerrero");
        };
    }

    public static String characterSlotName(String classId, int index) {
        String base = classLabel(classId);
        return base + " " + index;
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
        return format("Picked up %s", "Recogiste %s", itemId);
    }

    public static String shopCoins(int coins) {
        return LABEL_COINS + ": " + coins;
    }

    public static String shopEntry(String itemName, int price, int stock) {
        String stockLabel = stock < 0 ? "inf" : Integer.toString(stock);
        return itemName + "  $" + price + "  stk " + stockLabel;
    }

    public static String lootEntry(String itemName, int amount) {
        return itemName + "  " + itemCount(amount);
    }

    public static String shopPurchased(String itemName) {
        return STATUS_SHOP_PURCHASED + " " + itemName;
    }

    public static String languageOption() {
        return MENU_LANGUAGE + ": " + currentLanguageLabel();
    }

    public static String currentLanguageLabel() {
        return currentLanguage == UiLanguage.EN ? LABEL_LANGUAGE_EN : LABEL_LANGUAGE_ES;
    }

    public static List<String> keybindEntries() {
        return List.of(
                KEYBIND_MOVE,
                KEYBIND_INTERACT,
                KEYBIND_MELEE,
                KEYBIND_PROJECTILE,
                KEYBIND_INVENTORY,
                KEYBIND_CHARACTER,
                KEYBIND_QUICKSAVE,
                KEYBIND_MANUAL_SAVE,
                KEYBIND_OPTIONS,
                KEYBIND_DAY_NIGHT,
                MENU_BACK);
    }

    public static String loginSuccess(String accountLabel) {
        return LOGIN_SUCCESS + " " + accountLabel + ".";
    }

    public static String registerSuccess(String accountLabel) {
        return REGISTER_SUCCESS + " " + accountLabel + ".";
    }

    public static String confirmDeleteSave(String saveName) {
        return DIALOG_DELETE_SAVE + " \"" + saveName + "\"?";
    }

    private static Map<String, Object> loadBundle(String resourcePath) {
        try (InputStream input = ResourceStreams.open(UiText.class, resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Missing UI text resource: " + resourcePath);
            }
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return JsonDataLoader.parseObjectText(json);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load UI text resource: " + resourcePath, ex);
        }
    }

    private static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof String text) {
            return text;
        }
        throw new IllegalStateException("Missing UI text key: " + key + " for locale " + currentLanguage.code());
    }

    private static String format(String enPattern, String esPattern, Object... args) {
        return String.format(currentLanguage == UiLanguage.EN ? enPattern : esPattern, args);
    }
}
