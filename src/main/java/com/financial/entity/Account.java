package com.financial.entity;

import jakarta.persistence.*;
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
 * Account entity representing financial accounts in the money management system.
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AccountType type;
    
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", message = "Balance must be non-negative")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency code must be 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "JD";
    
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    @Column(name = "account_number", length = 50)
    private String accountNumber;
    
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Column(name = "color", length = 7)
    private String color; // Hex color code for UI display
    
    @Column(name = "icon", length = 50)
    private String icon; // Icon name for UI display
    
    @Column(name = "is_include_in_balance")
    private Boolean includeInBalance = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Enum representing different types of accounts.
     */
    public enum AccountType {
        WALLET("Wallet"),
        BANK_ACCOUNT("Bank Account"),
        SAVINGS("Savings"),
        CREDIT_CARD("Credit Card"),
        INVESTMENT("Investment"),
        CASH("Cash"),
        LOAN("Loan");
        
        private final String displayName;
        
        AccountType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Enum representing account status.
     */
    public enum AccountStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        CLOSED("Closed");
        
        private final String displayName;
        
        AccountStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
