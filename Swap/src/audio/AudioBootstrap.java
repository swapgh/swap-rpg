package audio;

public final class AudioBootstrap {
    private AudioBootstrap() {
    }

    public static AudioService createDefault() {
        AudioService audio = new AudioService();
        audio.registerEffect("pickup.coin", "/sound/coin.wav");
        audio.registerEffect("pickup.key", "/sound/powerup.wav");
        audio.registerEffect("door.open", "/sound/dooropen.wav");
        audio.registerEffect("door.locked", "/sound/blocked.wav");
        audio.registerEffect("dialogue.open", "/sound/speak.wav");
        audio.registerEffect("attack.swing", "/sound/swingweapon.wav");
        audio.registerEffect("attack.hit", "/sound/hitmonster.wav");
        audio.registerEffect("player.hurt", "/sound/receivedamage.wav");
        audio.registerEffect("projectile.cast", "/sound/burning.wav");
        audio.registerEffect("quest.complete", "/sound/levelup.wav");
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
