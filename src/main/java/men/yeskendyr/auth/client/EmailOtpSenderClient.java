package men.yeskendyr.auth.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "emailOtpSender", url = "${otp.email.url:}")
public interface EmailOtpSenderClient {
    @PostMapping("/send")
    void sendEmailOtp(@RequestBody EmailOtpRequest request);

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    @ToString
    class EmailOtpRequest {
        private String to;
        private String message;
    }
}
