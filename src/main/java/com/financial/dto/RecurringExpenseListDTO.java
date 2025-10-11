package com.financial.dto;

import com.financial.entity.RecurringExpense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lightweight DTO for recurring expense list views.
 * Contains only essential fields to reduce data transfer and avoid N+1 queries.
 */
@Data
@Builder
public class RecurringExpenseListDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private RecurringExpense.Frequency frequency;
    private LocalDate nextDueDate;
    private LocalDate lastPaidDate;
    private RecurringExpense.Status status;
    private Boolean isAutoPay;
    private String provider;
    
    // Minimal account information
    private Long accountId;
    private String accountName;
    
    // Minimal category information
    private Long categoryId;
    private String categoryName;
    
    /**
     * Factory method to create RecurringExpenseListDTO from RecurringExpense entity.
     *
     * @param expense the recurring expense entity
     * @return RecurringExpenseListDTO
     */
    public static RecurringExpenseListDTO fromEntity(RecurringExpense expense) {
        return RecurringExpenseListDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .frequency(expense.getFrequency())
                .nextDueDate(expense.getNextDueDate())
                .lastPaidDate(expense.getLastPaidDate())
                .status(expense.getStatus())
                .isAutoPay(expense.getIsAutoPay())
                .provider(expense.getProvider())
                .accountId(expense.getAccount() != null ? expense.getAccount().getId() : null)
                .accountName(expense.getAccount() != null ? expense.getAccount().getName() : null)
                .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .build();
    }
}

