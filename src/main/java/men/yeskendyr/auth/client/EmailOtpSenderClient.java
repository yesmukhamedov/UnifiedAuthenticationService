package men.yeskendyr.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "emailOtpSender", url = "${otp.email.url:}")
public interface EmailOtpSenderClient {
    @PostMapping("/send")
    void sendEmailOtp(@RequestBody EmailOtpRequest request);

    record EmailOtpRequest(String to, String message) {
    }
}
