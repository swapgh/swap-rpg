package data.world;

import component.progression.QuestComponent;
import data.EnemyData;

public record WorldPhaseData(
        String dayVisitQuestId,
        String nightVisitQuestId,
        SlimePhaseRuleData daySlime,
        SlimePhaseRuleData nightSlime) {

    public void updateVisitQuestAvailability(QuestComponent quests, boolean dayPhase) {
        if (dayPhase) {
            quests.setAvailable(nightVisitQuestId);
            quests.activate(dayVisitQuestId);
            return;
        }
        quests.setAvailable(dayVisitQuestId);
        quests.activate(nightVisitQuestId);
    }

    public String visitQuestForNpc(String npcType, boolean dayPhase) {
        if (dayPhase && "merchant".equals(npcType)) {
            return dayVisitQuestId;
        }
        if (!dayPhase && "old_man".equals(npcType)) {
            return nightVisitQuestId;
        }
        return null;
    }

    public SlimePhaseProfile slimeProfile(EnemyData baseSlime, boolean dayPhase) {
        SlimePhaseRuleData rule = dayPhase ? daySlime : nightSlime;
        return new SlimePhaseProfile(
                rule.displayName().isBlank() ? baseSlime.name() : rule.displayName(),
                baseSlime.stats().health() + rule.healthDelta(),
                baseSlime.stats().attack() + rule.attackDelta(),
                baseSlime.stats().defense() + rule.defenseDelta(),
                rule.animationBaseClipId());
    }

    public record SlimePhaseRuleData(
            String displayName,
            int healthDelta,
            int attackDelta,
            int defenseDelta,
            String animationBaseClipId) {
    }

    public record SlimePhaseProfile(
            String displayName,
            int maxHealth,
            int attack,
            int defense,
            String animationBaseClipId) {
    }
}
