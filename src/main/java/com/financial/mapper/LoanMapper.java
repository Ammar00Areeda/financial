package com.financial.mapper;

import com.financial.dto.LoanDto;
import com.financial.entity.Loan;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Loan entity and LoanDto.
 */
@Component
public class LoanMapper {
    
    public LoanDto toDto(Loan loan) {
        if (loan == null) {
            return null;
        }
        
        return LoanDto.builder()
                .id(loan.getId())
                .personName(loan.getPersonName())
                .phoneNumber(loan.getPhoneNumber())
                .email(loan.getEmail())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .totalAmount(loan.getTotalAmount())
                .paidAmount(loan.getPaidAmount())
                .remainingAmount(loan.getRemainingAmount())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .lastPaymentDate(loan.getLastPaymentDate())
                .status(loan.getStatus())
                .description(loan.getDescription())
                .notes(loan.getNotes())
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null)
                .accountName(loan.getAccount() != null ? loan.getAccount().getName() : null)
                .isUrgent(loan.getIsUrgent())
                .reminderEnabled(loan.getReminderEnabled())
                .nextReminderDate(loan.getNextReminderDate())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
    
    public Loan toEntity(LoanDto loanDto) {
        if (loanDto == null) {
            return null;
        }
        
        return Loan.builder()
                .id(loanDto.getId())
                .personName(loanDto.getPersonName())
                .phoneNumber(loanDto.getPhoneNumber())
                .email(loanDto.getEmail())
                .loanType(loanDto.getLoanType())
                .principalAmount(loanDto.getPrincipalAmount())
                .interestRate(loanDto.getInterestRate())
                .totalAmount(loanDto.getTotalAmount())
                .paidAmount(loanDto.getPaidAmount())
                .remainingAmount(loanDto.getRemainingAmount())
                .loanDate(loanDto.getLoanDate())
                .dueDate(loanDto.getDueDate())
                .lastPaymentDate(loanDto.getLastPaymentDate())
                .status(loanDto.getStatus())
                .description(loanDto.getDescription())
                .notes(loanDto.getNotes())
                // Note: Account will be set separately in the service
                .isUrgent(loanDto.getIsUrgent())
                .reminderEnabled(loanDto.getReminderEnabled())
                .nextReminderDate(loanDto.getNextReminderDate())
                .createdAt(loanDto.getCreatedAt())
                .updatedAt(loanDto.getUpdatedAt())
                .build();
    }
    
    public void updateEntityFromDto(Loan entity, LoanDto dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setPersonName(dto.getPersonName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());
        entity.setLoanType(dto.getLoanType());
        entity.setPrincipalAmount(dto.getPrincipalAmount());
        entity.setInterestRate(dto.getInterestRate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setPaidAmount(dto.getPaidAmount());
        entity.setRemainingAmount(dto.getRemainingAmount());
        entity.setLoanDate(dto.getLoanDate());
        entity.setDueDate(dto.getDueDate());
        entity.setLastPaymentDate(dto.getLastPaymentDate());
        entity.setStatus(dto.getStatus());
        entity.setDescription(dto.getDescription());
        entity.setNotes(dto.getNotes());
        entity.setIsUrgent(dto.getIsUrgent());
        entity.setReminderEnabled(dto.getReminderEnabled());
        entity.setNextReminderDate(dto.getNextReminderDate());
        // Note: Account will be set separately in the service
    }
}
