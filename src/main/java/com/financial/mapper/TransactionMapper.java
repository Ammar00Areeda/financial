package com.financial.mapper;

import com.financial.dto.TransactionDto;
import com.financial.entity.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Transaction entity and TransactionDto.
 */
@Component
public class TransactionMapper {
    
    public TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionDto.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getName() : null)
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .transferToAccountId(transaction.getTransferToAccount() != null ? transaction.getTransferToAccount().getId() : null)
                .transferToAccountName(transaction.getTransferToAccount() != null ? transaction.getTransferToAccount().getName() : null)
                .transactionDate(transaction.getTransactionDate())
                .notes(transaction.getNotes())
                .location(transaction.getLocation())
                .referenceNumber(transaction.getReferenceNumber())
                .isRecurring(transaction.getIsRecurring())
                .recurringFrequency(transaction.getRecurringFrequency())
                .recurringEndDate(transaction.getRecurringEndDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
    
    public Transaction toEntity(TransactionDto transactionDto) {
        if (transactionDto == null) {
            return null;
        }
        
        return Transaction.builder()
                .id(transactionDto.getId())
                .description(transactionDto.getDescription())
                .amount(transactionDto.getAmount())
                .type(transactionDto.getType())
                // Note: Account, Category, and TransferToAccount will be set separately in the service
                .transactionDate(transactionDto.getTransactionDate())
                .notes(transactionDto.getNotes())
                .location(transactionDto.getLocation())
                .referenceNumber(transactionDto.getReferenceNumber())
                .isRecurring(transactionDto.getIsRecurring())
                .recurringFrequency(transactionDto.getRecurringFrequency())
                .recurringEndDate(transactionDto.getRecurringEndDate())
                .createdAt(transactionDto.getCreatedAt())
                .updatedAt(transactionDto.getUpdatedAt())
                .build();
    }
    
    public void updateEntityFromDto(Transaction entity, TransactionDto dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setType(dto.getType());
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setNotes(dto.getNotes());
        entity.setLocation(dto.getLocation());
        entity.setReferenceNumber(dto.getReferenceNumber());
        entity.setIsRecurring(dto.getIsRecurring());
        entity.setRecurringFrequency(dto.getRecurringFrequency());
        entity.setRecurringEndDate(dto.getRecurringEndDate());
        // Note: Account, Category, and TransferToAccount will be set separately in the service
    }
}