package com.financial.mapper;

import com.financial.dto.AccountDto;
import com.financial.entity.Account;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Account entity and AccountDto.
 */
@Component
public class AccountMapper {
    
    public AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }
        
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .description(account.getDescription())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountNumber(account.getAccountNumber())
                .bankName(account.getBankName())
                .status(account.getStatus())
                .color(account.getColor())
                .icon(account.getIcon())
                .isIncludeInBalance(account.getIncludeInBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    public Account toEntity(AccountDto accountDto) {
        if (accountDto == null) {
            return null;
        }
        
        return Account.builder()
                .id(accountDto.getId())
                .name(accountDto.getName())
                .description(accountDto.getDescription())
                .type(accountDto.getType())
                .balance(accountDto.getBalance())
                .currency(accountDto.getCurrency())
                .accountNumber(accountDto.getAccountNumber())
                .bankName(accountDto.getBankName())
                .status(accountDto.getStatus())
                .color(accountDto.getColor())
                .icon(accountDto.getIcon())
                .includeInBalance(accountDto.getIsIncludeInBalance())
                .createdAt(accountDto.getCreatedAt())
                .updatedAt(accountDto.getUpdatedAt())
                .build();
    }
    
    public void updateEntityFromDto(Account entity, AccountDto dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setBalance(dto.getBalance());
        entity.setCurrency(dto.getCurrency());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setBankName(dto.getBankName());
        entity.setStatus(dto.getStatus());
        entity.setColor(dto.getColor());
        entity.setIcon(dto.getIcon());
        entity.setIncludeInBalance(dto.getIsIncludeInBalance());
    }
}


