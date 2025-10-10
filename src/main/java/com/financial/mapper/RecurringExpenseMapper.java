package com.financial.mapper;

import com.financial.dto.RecurringExpenseDto;
import com.financial.entity.RecurringExpense;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between RecurringExpense entity and RecurringExpenseDto.
 */
@Component
public class RecurringExpenseMapper {
    
    public RecurringExpenseDto toDto(RecurringExpense recurringExpense) {
        if (recurringExpense == null) {
            return null;
        }
        
        return RecurringExpenseDto.builder()
                .id(recurringExpense.getId())
                .name(recurringExpense.getName())
                .description(recurringExpense.getDescription())
                .amount(recurringExpense.getAmount())
                .frequency(recurringExpense.getFrequency())
                .accountId(recurringExpense.getAccount() != null ? recurringExpense.getAccount().getId() : null)
                .accountName(recurringExpense.getAccount() != null ? recurringExpense.getAccount().getName() : null)
                .categoryId(recurringExpense.getCategory() != null ? recurringExpense.getCategory().getId() : null)
                .categoryName(recurringExpense.getCategory() != null ? recurringExpense.getCategory().getName() : null)
                .startDate(recurringExpense.getStartDate())
                .endDate(recurringExpense.getEndDate())
                .nextDueDate(recurringExpense.getNextDueDate())
                .lastPaidDate(recurringExpense.getLastPaidDate())
                .status(recurringExpense.getStatus())
                .isAutoPay(recurringExpense.getIsAutoPay())
                .reminderDaysBefore(recurringExpense.getReminderDaysBefore())
                .provider(recurringExpense.getProvider())
                .referenceNumber(recurringExpense.getReferenceNumber())
                .notes(recurringExpense.getNotes())
                .createdAt(recurringExpense.getCreatedAt())
                .updatedAt(recurringExpense.getUpdatedAt())
                .build();
    }
    
    public RecurringExpense toEntity(RecurringExpenseDto recurringExpenseDto) {
        if (recurringExpenseDto == null) {
            return null;
        }
        
        return RecurringExpense.builder()
                .id(recurringExpenseDto.getId())
                .name(recurringExpenseDto.getName())
                .description(recurringExpenseDto.getDescription())
                .amount(recurringExpenseDto.getAmount())
                .frequency(recurringExpenseDto.getFrequency())
                // Note: Account and Category will be set separately in the service
                .startDate(recurringExpenseDto.getStartDate())
                .endDate(recurringExpenseDto.getEndDate())
                .nextDueDate(recurringExpenseDto.getNextDueDate())
                .lastPaidDate(recurringExpenseDto.getLastPaidDate())
                .status(recurringExpenseDto.getStatus())
                .isAutoPay(recurringExpenseDto.getIsAutoPay())
                .reminderDaysBefore(recurringExpenseDto.getReminderDaysBefore())
                .provider(recurringExpenseDto.getProvider())
                .referenceNumber(recurringExpenseDto.getReferenceNumber())
                .notes(recurringExpenseDto.getNotes())
                .createdAt(recurringExpenseDto.getCreatedAt())
                .updatedAt(recurringExpenseDto.getUpdatedAt())
                .build();
    }
    
    public void updateEntityFromDto(RecurringExpense entity, RecurringExpenseDto dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setFrequency(dto.getFrequency());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setNextDueDate(dto.getNextDueDate());
        entity.setLastPaidDate(dto.getLastPaidDate());
        entity.setStatus(dto.getStatus());
        entity.setIsAutoPay(dto.getIsAutoPay());
        entity.setReminderDaysBefore(dto.getReminderDaysBefore());
        entity.setProvider(dto.getProvider());
        entity.setReferenceNumber(dto.getReferenceNumber());
        entity.setNotes(dto.getNotes());
        // Note: Account and Category will be set separately in the service
    }
}
