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
 * Loan entity representing money lent to or borrowed from friends and family.
 */
@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Borrower/Lender name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "person_name", nullable = false, length = 100)
    private String personName; // Name of the person (friend/family member)
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;
    
    @NotNull(message = "Loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false)
    private LoanType loanType;
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.01", message = "Principal amount must be greater than 0")
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate; // Annual interest rate (optional)
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount; // Principal + Interest
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount;
    
    @NotNull(message = "Loan date is required")
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account; // Account used for the loan transaction
    
    @Column(name = "is_urgent")
    @Builder.Default
    private Boolean isUrgent = false;
    
    @Column(name = "reminder_enabled")
    @Builder.Default
    private Boolean reminderEnabled = true;
    
    @Column(name = "next_reminder_date")
    private LocalDateTime nextReminderDate;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (remainingAmount == null) {
            remainingAmount = totalAmount != null ? totalAmount : principalAmount;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Recalculate remaining amount
        if (totalAmount != null && paidAmount != null) {
            remainingAmount = totalAmount.subtract(paidAmount);
        }
    }
    
    /**
     * Enum representing different types of loans.
     */
    public enum LoanType {
        LENT("Lent Money"), // You lent money to someone
        BORROWED("Borrowed Money"); // You borrowed money from someone
        
        private final String displayName;
        
        LoanType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Enum representing loan status.
     */
    public enum LoanStatus {
        ACTIVE("Active"),
        PAID_OFF("Paid Off"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        PARTIALLY_PAID("Partially Paid");
        
        private final String displayName;
        
        LoanStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Calculate the percentage of loan paid.
     *
     * @return percentage paid (0-100)
     */
    public BigDecimal getPercentagePaid() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return paidAmount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Check if the loan is overdue.
     *
     * @return true if overdue, false otherwise
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && status == LoanStatus.ACTIVE;
    }
    
    /**
     * Check if the loan is fully paid.
     *
     * @return true if fully paid, false otherwise
     */
    public boolean isFullyPaid() {
        return remainingAmount != null && remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
    }
}
