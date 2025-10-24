package com.financial.dto;

import com.financial.entity.RecurringExpense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for RecurringExpense responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseResponseDto {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal amount;
    private RecurringExpense.Frequency frequency;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDueDate;
    private LocalDate lastPaidDate;
    private RecurringExpense.Status status;
    private Boolean isAutoPay;
    private Integer reminderDaysBefore;
    private String provider;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Create response DTO from RecurringExpense entity.
     */
    public static RecurringExpenseResponseDto fromEntity(RecurringExpense expense) {
        if (expense == null) {
            return null;
        }
        return RecurringExpenseResponseDto.builder()
                .id(expense.getId())
                .name(expense.getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .frequency(expense.getFrequency())
                .accountId(expense.getAccount() != null ? expense.getAccount().getId() : null)
                .accountName(expense.getAccount() != null ? expense.getAccount().getName() : null)
                .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .startDate(expense.getStartDate())
                .endDate(expense.getEndDate())
                .nextDueDate(expense.getNextDueDate())
                .lastPaidDate(expense.getLastPaidDate())
                .status(expense.getStatus())
                .isAutoPay(expense.getIsAutoPay())
                .reminderDaysBefore(expense.getReminderDaysBefore())
                .provider(expense.getProvider())
                .referenceNumber(expense.getReferenceNumber())
                .notes(expense.getNotes())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}




