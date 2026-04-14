package system.interaction;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

import audio.AudioService;
import app.input.KeyboardState;
import component.character.DialogueComponent;
import component.character.FacingComponent;
import component.character.InputComponent;
import component.character.NameComponent;
import component.character.NpcComponent;
import component.character.PlayerComponent;
import component.progression.QuestComponent;
import component.progression.ShopComponent;
import component.world.ColliderComponent;
import component.world.PositionComponent;
import component.world.WorldTimeComponent;
import data.DataRegistry;
import ecs.EcsWorld;
import state.GameMode;
import ui.state.UiState;
import ui.text.ContentText;
import ui.text.UiText;
import util.CollisionUtil;

final class NpcInteractionSystem {
    private final UiState ui;
    private final AudioService audio;
    private final KeyboardState keyboard;
    private final DataRegistry data;
    private final InteractionSupport support;

    NpcInteractionSystem(UiState ui, AudioService audio, KeyboardState keyboard, DataRegistry data, InteractionSupport support) {
        this.ui = ui;
        this.audio = audio;
        this.keyboard = keyboard;
        this.data = data;
        this.support = support;
    }

    boolean handle(EcsWorld world) {
        if (ui.mode == GameMode.DIALOGUE) {
            ui.contextHint = UiText.WORLD_HINT_CONTINUE;
            int player = world.entitiesWith(PlayerComponent.class).get(0);
            InputComponent input = world.require(player, InputComponent.class);
            if (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)) {
                closeDialogue();
                return true;
            }
            if (!input.interactPressed) {
                return true;
            }
            closeDialogue();
            return true;
        }

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent playerPos = world.require(player, PositionComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        InputComponent input = world.require(player, InputComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        boolean dayPhase = isDay(world);
        Rectangle interactRect = support.interactionRect(playerPos, playerCollider, world.require(player, FacingComponent.class).direction);

        for (int npc : world.entitiesWith(NpcComponent.class, DialogueComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle npcRect = CollisionUtil.rect(world.require(npc, PositionComponent.class),
                    world.require(npc, ColliderComponent.class));
            if (!interactRect.intersects(npcRect)) {
                continue;
            }
            ui.contextHint = world.has(npc, ShopComponent.class) ? UiText.WORLD_HINT_OPEN_SHOP : UiText.WORLD_HINT_TALK;
            if (!input.interactPressed) {
                return true;
            }
            if (world.has(npc, ShopComponent.class)) {
                ui.mode = GameMode.SHOP;
                ui.shopNpcEntity = npc;
                ui.shopSpeaker = world.require(npc, NameComponent.class).value;
                ui.shopStatusMessage = "";
                ui.shopSelectedIndex = 0;
                return true;
            }
            audio.playEffect("dialogue.open");
            NpcComponent npcComponent = world.require(npc, NpcComponent.class);
            ui.mode = GameMode.DIALOGUE;
            ui.dialogueSpeaker = ContentText.text(data.npc(npcComponent.npcType).nameKey());
            ui.dialogueLines = ContentText.lines(data.npc(npcComponent.npcType).dialogueKeysForPhase(dayPhase));
            completeNpcTimeQuest(quests, npcComponent.npcType, dayPhase);
            return true;
        }

        return false;
    }

    private boolean isDay(EcsWorld world) {
        List<Integer> times = world.entitiesWith(WorldTimeComponent.class);
        return times.isEmpty() || world.require(times.get(0), WorldTimeComponent.class).isDay();
    }

    private void completeNpcTimeQuest(QuestComponent quests, String npcType, boolean dayPhase) {
        String questId = data.worldPhase().visitQuestForNpc(npcType, dayPhase);
        if (questId == null || !quests.complete(questId)) {
            return;
        }
        audio.playEffect("quest.complete");
        ui.pushToast(ContentText.text(data.quest(questId).completionToastKey()), 120);
    }

    private void closeDialogue() {
        ui.mode = GameMode.PLAY;
        ui.dialogueSpeaker = "";
        ui.dialogueLines = new String[0];
    }
}
