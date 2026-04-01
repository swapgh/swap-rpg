package component.world;

public final class WorldObjectComponent {
    public final String objectId;
    public final String interactionHintKey;
    public final String successToastKey;
    public final String failureToastKey;
    public final String successAudioId;
    public final String failureAudioId;

    public WorldObjectComponent(String objectId, String interactionHintKey, String successToastKey, String failureToastKey,
            String successAudioId, String failureAudioId) {
        this.objectId = objectId;
        this.interactionHintKey = interactionHintKey;
        this.successToastKey = successToastKey;
        this.failureToastKey = failureToastKey;
        this.successAudioId = successAudioId;
        this.failureAudioId = failureAudioId;
    }
}
