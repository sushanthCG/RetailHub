package com.application.RetailHub.Services;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.application.RetailHub.Entities.JwtToken;
import com.application.RetailHub.Entities.Otp;
import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Repositories.JwtTokenRepository;
import com.application.RetailHub.Repositories.OtpRepository;
import com.application.RetailHub.Repositories.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Key SIGNING_KEY;

    @Autowired
    public AuthService(UserRepository userRepository,
                       OtpRepository otpRepository,
                       JwtTokenRepository jwtTokenRepository,
                       JavaMailSender mailSender,
                       @Value("${jwt.secret}") String jwtSecret) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = new BCryptPasswordEncoder();

        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException("JWT_SECRET must be at least 64 bytes long.");
        }

        this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // =====================================================
    // 🔐 NORMAL LOGIN
    // =====================================================
    public String loginAndGenerateToken(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return generateToken(user);
    }

    // =====================================================
    // 🔑 FORGOT PASSWORD - GENERATE OTP
    // =====================================================
    public User generatePasswordResetOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // ✅ Delete old OTPs for this user before creating new one
        otpRepository.deleteByUserId(user.getUser_id());

        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);

        Otp otp = new Otp();
        otp.setOtpCode(otpCode);
        otp.setUser(user);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Password Reset OTP");
        mail.setText("Your OTP is: " + otpCode + " (Valid for 5 minutes)");
        mailSender.send(mail);

        return user;
    }

    // =====================================================
    // ✅ VERIFY RESET OTP
    // =====================================================
    public void verifyResetOtp(String email, String enteredOtp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

     // ✅ Must match new method name
        Otp otp = otpRepository.findLatestOtpByUserId(user.getUser_id())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
			throw new RuntimeException("OTP expired");
		}

        if (!otp.getOtpCode().equals(enteredOtp)) {
			throw new RuntimeException("Invalid OTP");
		}
    }

    // =====================================================
    // 🔄 RESET PASSWORD
    // =====================================================
    public void resetPassword(String email, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // =====================================================
    // 🎟 JWT TOKEN GENERATION
    // =====================================================
    private String generateToken(User user) {

        LocalDateTime now = LocalDateTime.now();
        JwtToken existingToken = jwtTokenRepository.findByUserId(user.getUser_id());

        if (existingToken != null && now.isBefore(existingToken.getExpires_at())) {
            return existingToken.getToken();
        }

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();

        if (existingToken != null) {
            jwtTokenRepository.delete(existingToken);
        }

        JwtToken jwtToken = new JwtToken(user, token, LocalDateTime.now().plusHours(1));
        jwtTokenRepository.save(jwtToken);

        return token;
    }
}