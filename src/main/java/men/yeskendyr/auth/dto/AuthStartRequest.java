package men.yeskendyr.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record AuthStartRequest(
        @Email(message = "email must be valid")
        String email,
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phoneNumber must be valid")
        String phoneNumber
) {
    @AssertTrue(message = "either email or phoneNumber must be provided")
    public boolean isValid() {
        return (email != null && phoneNumber == null) || (email == null && phoneNumber != null);
    }
}
