package com.financial.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
 * DTO for creating a new loan.
 * Contains only fields that can be provided by the user during creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreateRequestDto {
    
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
    
    @JsonAlias("amount")
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "0.01", message = "Principal amount must be greater than 0")
    private BigDecimal principalAmount;
    
    @DecimalMin(value = "0.00", message = "Interest rate must be greater than or equal to 0")
    private BigDecimal interestRate;
    
    @NotNull(message = "Loan date is required")
    private LocalDateTime loanDate;
    
    private LocalDateTime dueDate;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @Builder.Default
    private Boolean isUrgent = false;
    
    @Builder.Default
    private Boolean reminderEnabled = true;
    
    private LocalDateTime nextReminderDate;
}


