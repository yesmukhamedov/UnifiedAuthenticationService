package men.yeskendyr.auth.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import men.yeskendyr.auth.config.JwtProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class JwtKeyService {
    private final RSAPublicKey publicKey;
    private final PrivateKey privateKey;
    private final String keyId;

    public JwtKeyService(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        KeyPair keyPair = loadKeyPair(jwtProperties, resourceLoader);
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = keyPair.getPrivate();
        this.keyId = deriveKeyId(this.publicKey);
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getKeyId() {
        return keyId;
    }

    private KeyPair loadKeyPair(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        String privatePem = resolvePem(jwtProperties.getPrivateKeyPem(),
                jwtProperties.getPrivateKeyLocation(), resourceLoader);
        String publicPem = resolvePem(jwtProperties.getPublicKeyPem(),
                jwtProperties.getPublicKeyLocation(), resourceLoader);
        if (privatePem != null && publicPem != null) {
            try {
                PrivateKey privateKey = parsePrivateKey(privatePem);
                PublicKey publicKey = parsePublicKey(publicPem);
                return new KeyPair(publicKey, privateKey);
            } catch (GeneralSecurityException ex) {
                throw new IllegalStateException("Unable to parse configured JWT key pair", ex);
            }
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to generate JWT key pair", ex);
        }
    }

    private String resolvePem(String inlinePem, String location, ResourceLoader resourceLoader) {
        if (inlinePem != null && !inlinePem.isBlank()) {
            return inlinePem;
        }
        if (location == null || location.isBlank()) {
            return null;
        }
        Resource resource = resourceLoader.getResource(location);
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read key material from " + location, ex);
        }
    }

    private PrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
        String content = stripPem(pem);
        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey parsePublicKey(String pem) throws GeneralSecurityException {
        String content = stripPem(pem);
        byte[] decoded = Base64.getDecoder().decode(content);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String stripPem(String pem) {
        return pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    private String deriveKeyId(RSAPublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(publicKey.getEncoded());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to derive key id", ex);
        }
    }
}
