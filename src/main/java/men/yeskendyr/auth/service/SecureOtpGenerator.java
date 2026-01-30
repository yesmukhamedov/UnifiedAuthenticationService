package men.yeskendyr.auth.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class SecureOtpGenerator implements OtpGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
