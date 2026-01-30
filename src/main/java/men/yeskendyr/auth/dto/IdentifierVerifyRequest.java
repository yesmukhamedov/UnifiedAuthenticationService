package men.yeskendyr.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record IdentifierVerifyRequest(
        @NotNull UUID challengeId,
        @NotBlank
        @Pattern(regexp = "^[0-9]{4,8}$", message = "code must be numeric")
        String code
) {
}
