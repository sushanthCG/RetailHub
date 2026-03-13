package com.application.RetailHub.Services;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.application.RetailHub.Repositories.OtpRepository;

@Service
public class OtpCleanupService {

    private final OtpRepository otpRepository;

    public OtpCleanupService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

 
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}