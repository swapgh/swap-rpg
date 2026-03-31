package audio;

public final class AudioBootstrap {
    private AudioBootstrap() {
    }

    public static AudioService createDefault() {
        AudioService audio = new AudioService();
        audio.registerEffect("pickup.coin", "/audio/sfx/world/coin.wav");
        audio.registerEffect("pickup.key", "/audio/sfx/world/powerup.wav");
        audio.registerEffect("door.open", "/audio/sfx/world/dooropen.wav");
        audio.registerEffect("door.locked", "/audio/sfx/world/blocked.wav");
        audio.registerEffect("dialogue.open", "/audio/sfx/dialogue/speak.wav");
        audio.registerEffect("attack.swing", "/audio/sfx/combat/swingweapon.wav");
        audio.registerEffect("attack.hit", "/audio/sfx/combat/hitmonster.wav");
        audio.registerEffect("player.hurt", "/audio/sfx/combat/receivedamage.wav");
        audio.registerEffect("projectile.cast", "/audio/sfx/combat/burning.wav");
        audio.registerEffect("quest.complete", "/audio/sfx/quest/levelup.wav");
        return audio;
    }

    public static void prewarmWorldEffects(AudioService audio) {
        audio.prewarmEffects(
                "attack.swing",
                "attack.hit",
                "player.hurt",
                "projectile.cast",
                "pickup.coin",
                "pickup.key",
                "door.open",
                "door.locked",
                "dialogue.open",
                "quest.complete");
    }
}
