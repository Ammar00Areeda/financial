package com.financial.dto;

import com.financial.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for transaction list views.
 * Contains only essential fields to reduce data transfer and avoid N+1 queries.
 */
@Data
@Builder
public class TransactionListDTO {
    
    private Long id;
    private String description;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private LocalDateTime transactionDate;
    
    // Minimal account information
    private Long accountId;
    private String accountName;
    
    // Minimal category information
    private Long categoryId;
    private String categoryName;
    
    // Transfer information (if applicable)
    private Long transferToAccountId;
    private String transferToAccountName;
    
    private String location;
    private Boolean isRecurring;
    
    /**
     * Factory method to create TransactionListDTO from Transaction entity.
     *
     * @param transaction the transaction entity
     * @return TransactionListDTO
     */
    public static TransactionListDTO fromEntity(Transaction transaction) {
        return TransactionListDTO.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountName(transaction.getAccount() != null ? transaction.getAccount().getName() : null)
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .transferToAccountId(transaction.getTransferToAccount() != null ? transaction.getTransferToAccount().getId() : null)
                .transferToAccountName(transaction.getTransferToAccount() != null ? transaction.getTransferToAccount().getName() : null)
                .location(transaction.getLocation())
                .isRecurring(transaction.getIsRecurring())
                .build();
    }
}

