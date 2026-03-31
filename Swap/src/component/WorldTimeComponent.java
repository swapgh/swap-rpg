package component;

public final class WorldTimeComponent {
    public static final int SECONDS_PER_DAY = 24 * 60 * 60;

    public long totalSeconds;
    public double secondProgress;
    public long lastRealEpochSeconds;

    public WorldTimeComponent(long totalSeconds, long lastRealEpochSeconds) {
        this.totalSeconds = totalSeconds;
        this.lastRealEpochSeconds = lastRealEpochSeconds;
    }

    public int dayNumber() {
        return (int) (totalSeconds / SECONDS_PER_DAY) + 1;
    }

    public int secondOfDay() {
        return (int) Math.floorMod(totalSeconds, SECONDS_PER_DAY);
    }

    public int hour() {
        return secondOfDay() / 3600;
    }

    public int minute() {
        return (secondOfDay() / 60) % 60;
    }

    public int second() {
        return secondOfDay() % 60;
    }

    public boolean isDay() {
        int hour = hour();
        return hour >= 6 && hour < 18;
    }

    public void setSecondOfDay(int secondOfDay) {
        long dayBase = totalSeconds - secondOfDay();
        totalSeconds = dayBase + Math.floorMod(secondOfDay, SECONDS_PER_DAY);
        secondProgress = 0;
    }
}
