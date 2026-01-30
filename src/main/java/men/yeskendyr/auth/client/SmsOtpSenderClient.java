package men.yeskendyr.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "smsOtpSender", url = "${otp.sms.url:}")
public interface SmsOtpSenderClient {
    @PostMapping("/send")
    void sendSmsOtp(@RequestBody SmsOtpRequest request);

    record SmsOtpRequest(String to, String message) {
    }
}
