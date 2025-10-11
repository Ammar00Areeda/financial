package com.financial.dto;

import com.financial.entity.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Lightweight DTO for account list views.
 * Contains only essential fields to reduce data transfer.
 */
@Data
@Builder
public class AccountListDTO {
    
    private Long id;
    private String name;
    private Account.AccountType type;
    private BigDecimal balance;
    private String currency;
    private Account.AccountStatus status;
    private String color;
    private String icon;
    private Boolean includeInBalance;
    
    /**
     * Factory method to create AccountListDTO from Account entity.
     *
     * @param account the account entity
     * @return AccountListDTO
     */
    public static AccountListDTO fromEntity(Account account) {
        return AccountListDTO.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .color(account.getColor())
                .icon(account.getIcon())
                .includeInBalance(account.getIncludeInBalance())
                .build();
    }
}

