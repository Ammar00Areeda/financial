package com.financial.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.financial.entity.Transaction;
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
 * DTO for Transaction entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {
    
    private Long id;
    
    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;
    
    @NotNull(message = "Account is required")
    private Long accountId;
    
    private String accountName; // For display purposes
    
    private Long categoryId;
    
    private String categoryName; // For display purposes
    
    private Long transferToAccountId;
    
    private String transferToAccountName; // For display purposes
    
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    private String referenceNumber;
    
    private Boolean isRecurring;
    
    private Transaction.RecurringFrequency recurringFrequency;
    
    private LocalDateTime recurringEndDate;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}


