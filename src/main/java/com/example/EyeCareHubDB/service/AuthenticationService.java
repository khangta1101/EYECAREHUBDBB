package com.example.EyeCareHubDB.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.EyeCareHubDB.dto.AccountResponse;
import com.example.EyeCareHubDB.dto.ForgotPasswordRequest;
import com.example.EyeCareHubDB.dto.LoginRequest;
import com.example.EyeCareHubDB.dto.RegisterRequest;
import com.example.EyeCareHubDB.dto.ResetPasswordRequest;
import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.Customer;
import com.example.EyeCareHubDB.repository.AccountRepository;
import com.example.EyeCareHubDB.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int OTP_LENGTH = 6;
    
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final JavaMailSender mailSender;
    
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    
    public Account register(RegisterRequest request) {
        // Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        
        // Create new account
        Account account = Account.builder()
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // TODO: Hash password properly with BCrypt
                .phoneNumber(request.getPhoneNumber())
                .role(Account.AccountRole.CUSTOMER)
                .status(Account.AccountStatus.ACTIVE)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        // Create customer profile
        if (request.getFirstName() != null || request.getLastName() != null) {
            Customer customer = Customer.builder()
                    .account(savedAccount)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .build();
            customerRepository.save(customer);
        }
        
        return savedAccount;
    }
    
    public AccountResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found: " + request.getEmail()));
        
        // TODO: Verify password with BCrypt
        if (!account.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        if (account.getStatus() == Account.AccountStatus.DELETED || 
            account.getStatus() == Account.AccountStatus.SUSPENDED) {
            throw new RuntimeException("Account is not active");
        }
        
        // Update last login time
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);
        
        // TODO: Generate JWT token
        String token = generateToken();
        
        // Get customer info if exists
        Customer customer = customerRepository.findByAccountId(account.getId()).orElse(null);
        
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .role(account.getRole().name())
                .phoneNumber(account.getPhoneNumber())
                .firstName(customer != null ? customer.getFirstName() : null)
                .lastName(customer != null ? customer.getLastName() : null)
                .token(token)
                .message("Login successfully")
                .build();
    }
    
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        String email = request.getEmail().trim();
        accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found: " + email));

        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES);
        otpStore.put(email, new OtpEntry(otp, expiresAt));

        sendOtpEmail(email, otp, expiresAt);
    }
    
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            throw new RuntimeException("OTP is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("New password is required");
        }

        String email = request.getEmail().trim();
        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null) {
            throw new RuntimeException("OTP not found. Please request a new OTP");
        }
        if (LocalDateTime.now().isAfter(otpEntry.expiresAt())) {
            otpStore.remove(email);
            throw new RuntimeException("OTP expired. Please request a new OTP");
        }
        if (!otpEntry.otp().equals(request.getOtp().trim())) {
            throw new RuntimeException("Invalid OTP");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found: " + email));

        account.setPasswordHash(request.getNewPassword());
        accountRepository.save(account);
        otpStore.remove(email);
    }
    
    public Account createAccountByAdmin(RegisterRequest request) {
        return register(request);
    }
    
    private String generateToken() {
        // TODO: Implement JWT token generation
        return UUID.randomUUID().toString();
    }

    private String generateOtp() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, max + 1));
    }

    private void sendOtpEmail(String email, String otp, LocalDateTime expiresAt) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("EyeCareHub - Password Reset OTP");
            message.setText("Your OTP code is: " + otp + "\nThis OTP expires at: " + expiresAt + "\nIf you did not request this, please ignore this email.");
            mailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot send OTP email. Please check mail server configuration.");
        }
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt) {
    }
}
