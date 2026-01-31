package men.yeskendyr.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class AuthStartRequest {
    @Email(message = "email must be valid")
    private String email;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phoneNumber must be valid")
    private String phoneNumber;

    @AssertTrue(message = "either email or phoneNumber must be provided")
    public boolean isValid() {
        return (email != null && phoneNumber == null) || (email == null && phoneNumber != null);
    }
}
