package component;

public final class WorldObjectComponent {
    public final String objectId;
    public final String interactionHint;
    public final String successToast;
    public final String failureToast;
    public final String successAudioId;
    public final String failureAudioId;

    public WorldObjectComponent(String objectId, String interactionHint, String successToast, String failureToast,
            String successAudioId, String failureAudioId) {
        this.objectId = objectId;
        this.interactionHint = interactionHint;
        this.successToast = successToast;
        this.failureToast = failureToast;
        this.successAudioId = successAudioId;
        this.failureAudioId = failureAudioId;
    }
}
