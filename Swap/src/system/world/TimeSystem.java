package system.world;

import component.world.WorldTimeComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.time.Instant;

public final class TimeSystem implements EcsSystem {
    @Override
    public void update(EcsWorld world, double dtSeconds) {
        long nowEpochSeconds = Instant.now().getEpochSecond();
        for (int entity : world.entitiesWith(WorldTimeComponent.class)) {
            WorldTimeComponent time = world.require(entity, WorldTimeComponent.class);
            if (time.lastRealEpochSeconds > 0 && nowEpochSeconds > time.lastRealEpochSeconds) {
                time.totalSeconds += nowEpochSeconds - time.lastRealEpochSeconds;
                time.lastRealEpochSeconds = nowEpochSeconds;
                time.secondProgress = 0;
                continue;
            }

            time.secondProgress += dtSeconds;
            while (time.secondProgress >= 1.0) {
                time.totalSeconds++;
                time.secondProgress -= 1.0;
            }
            time.lastRealEpochSeconds = nowEpochSeconds;
        }
    }
}
