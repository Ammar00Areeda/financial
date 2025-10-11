package com.financial.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.financial.entity.Account;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Account entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {
    
    private Long id;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Account type is required")
    private Account.AccountType type;
    
    @NotNull(message = "Balance is required")
    private BigDecimal balance;
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;
    
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String accountNumber;
    
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;
    
    private Account.AccountStatus status;
    
    @Size(max = 7, message = "Color must not exceed 7 characters")
    private String color;
    
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;
    
    private Boolean includeInBalance;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}


