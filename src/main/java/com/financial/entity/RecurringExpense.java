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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RecurringExpense entity representing fixed monthly expenses like subscriptions.
 */
@Entity
@Table(name = "recurring_expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Expense name is required")
    @Size(max = 100, message = "Expense name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Frequency is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency;
    
    @NotNull(message = "Account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;
    
    @Column(name = "last_paid_date")
    private LocalDate lastPaidDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.ACTIVE;
    
    @Column(name = "is_auto_pay")
    private Boolean isAutoPay = false;
    
    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore = 3;
    
    @Size(max = 200, message = "Provider must not exceed 200 characters")
    @Column(name = "provider", length = 200)
    private String provider; // e.g., "YouTube", "Netflix", "Spotify"
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;
    
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
        if (nextDueDate == null) {
            nextDueDate = calculateNextDueDate(startDate, frequency);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate the next due date based on frequency.
     */
    public LocalDate calculateNextDueDate(LocalDate fromDate, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> fromDate.plusDays(1);
            case WEEKLY -> fromDate.plusWeeks(1);
            case MONTHLY -> fromDate.plusMonths(1);
            case QUARTERLY -> fromDate.plusMonths(3);
            case YEARLY -> fromDate.plusYears(1);
        };
    }
    
    /**
     * Check if the expense is due today.
     */
    public boolean isDueToday() {
        return nextDueDate != null && nextDueDate.isEqual(LocalDate.now());
    }
    
    /**
     * Check if the expense is overdue.
     */
    public boolean isOverdue() {
        return nextDueDate != null && nextDueDate.isBefore(LocalDate.now()) && status == Status.ACTIVE;
    }
    
    /**
     * Check if the expense is due soon (within reminder days).
     */
    public boolean isDueSoon() {
        if (nextDueDate == null || reminderDaysBefore == null) {
            return false;
        }
        LocalDate reminderDate = nextDueDate.minusDays(reminderDaysBefore);
        return LocalDate.now().isAfter(reminderDate) && !isOverdue();
    }
    
    /**
     * Mark as paid and calculate next due date.
     */
    public void markAsPaid() {
        lastPaidDate = LocalDate.now();
        nextDueDate = calculateNextDueDate(nextDueDate, frequency);
    }
    
    /**
     * Enum representing different frequencies.
     */
    public enum Frequency {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        QUARTERLY("Quarterly"),
        YEARLY("Yearly");
        
        private final String displayName;
        
        Frequency(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Enum representing recurring expense status.
     */
    public enum Status {
        ACTIVE("Active"),
        PAUSED("Paused"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
