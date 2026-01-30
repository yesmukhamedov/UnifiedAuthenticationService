package men.yeskendyr.auth.service;

import men.yeskendyr.auth.entity.IdentifierType;

public final class IdentifierNormalizer {
    private IdentifierNormalizer() {
    }

    public static String normalize(IdentifierType type, String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (type == IdentifierType.EMAIL) {
            return trimmed.toLowerCase();
        }
        return trimmed.replaceAll("\\s+", "");
    }
}
