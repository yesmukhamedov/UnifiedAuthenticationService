package men.yeskendyr.auth.dto;

import men.yeskendyr.auth.entity.IdentifierType;

public record IdentifierResponse(IdentifierType type, String value) {
}
