package men.yeskendyr.auth.service;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwksService {
    private final JwtKeyService jwtKeyService;

    public JwksService(JwtKeyService jwtKeyService) {
        this.jwtKeyService = jwtKeyService;
    }

    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = jwtKeyService.getPublicKey();
        Map<String, Object> key = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", jwtKeyService.getKeyId(),
                "n", toBase64Url(publicKey.getModulus()),
                "e", toBase64Url(publicKey.getPublicExponent())
        );
        return Map.of("keys", List.of(key));
    }

    public String getKeyId() {
        return jwtKeyService.getKeyId();
    }

    private String toBase64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
