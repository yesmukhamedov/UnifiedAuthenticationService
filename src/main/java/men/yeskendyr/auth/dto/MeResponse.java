package men.yeskendyr.auth.dto;

import java.util.List;
import java.util.UUID;

public record MeResponse(UUID userId, List<IdentifierResponse> identifiers) {
}
