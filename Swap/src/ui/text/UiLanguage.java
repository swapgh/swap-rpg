package ui.text;

public enum UiLanguage {
    EN("en", "/content/ui/text/en.json"),
    ES("es", "/content/ui/text/es.json");

    private final String code;
    private final String resourcePath;

    UiLanguage(String code, String resourcePath) {
        this.code = code;
        this.resourcePath = resourcePath;
    }

    public String code() {
        return code;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public UiLanguage next() {
        return this == EN ? ES : EN;
    }
}
