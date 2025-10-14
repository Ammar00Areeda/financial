package com.financial.dto;

import com.financial.entity.RecurringExpense;
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

/**
 * DTO for updating an existing recurring expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseUpdateRequestDto {
    
    @NotBlank(message = "Expense name is required")
    @Size(max = 100, message = "Expense name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Frequency is required")
    private RecurringExpense.Frequency frequency;
    
    @NotNull(message = "Account is required")
    private Long accountId;
    
    private Long categoryId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private RecurringExpense.Status status;
    
    private Boolean isAutoPay;
    
    private Integer reminderDaysBefore;
    
    @Size(max = 200, message = "Provider must not exceed 200 characters")
    private String provider;
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}



