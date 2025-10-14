package com.financial.dto;

import com.financial.entity.Loan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
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
 * DTO for updating an existing loan.
 * Contains only fields that can be modified by the user.
 * Note: Payment amounts should be updated via the payment endpoint, not here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanUpdateRequestDto {
    
    @NotBlank(message = "Person name is required")
    @Size(max = 100, message = "Person name must not exceed 100 characters")
    private String personName;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotNull(message = "Loan type is required")
    private Loan.LoanType loanType;
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.01", message = "Principal amount must be greater than 0")
    private BigDecimal principalAmount;
    
    @DecimalMin(value = "0.00", message = "Interest rate must be greater than or equal to 0")
    private BigDecimal interestRate;
    
    @NotNull(message = "Loan date is required")
    private LocalDateTime loanDate;
    
    private LocalDateTime dueDate;
    
    @NotNull(message = "Loan status is required")
    private Loan.LoanStatus status;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    private Long accountId;
    
    private Boolean isUrgent;
    
    private Boolean reminderEnabled;
    
    private LocalDateTime nextReminderDate;
}



