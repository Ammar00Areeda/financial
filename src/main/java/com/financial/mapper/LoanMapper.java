package com.financial.mapper;

import com.financial.dto.LoanCreateRequestDto;
import com.financial.dto.LoanDto;
import com.financial.dto.LoanResponseDto;
import com.financial.dto.LoanUpdateRequestDto;
import com.financial.entity.Loan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for converting between Loan entity and DTOs.
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
    
    // ========== NEW DTO MAPPING METHODS ==========
    
    /**
     * Convert LoanCreateRequestDto to Loan entity.
     *
     * @param requestDto the create request DTO
     * @return Loan entity
     */
    public Loan toEntityFromCreateRequest(LoanCreateRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        
        return Loan.builder()
                .personName(requestDto.getPersonName())
                .phoneNumber(requestDto.getPhoneNumber())
                .email(requestDto.getEmail())
                .loanType(requestDto.getLoanType())
                .principalAmount(requestDto.getPrincipalAmount())
                .interestRate(requestDto.getInterestRate())
                .loanDate(requestDto.getLoanDate())
                .dueDate(requestDto.getDueDate())
                .description(requestDto.getDescription())
                .notes(requestDto.getNotes())
                .isUrgent(requestDto.getIsUrgent())
                .reminderEnabled(requestDto.getReminderEnabled())
                .nextReminderDate(requestDto.getNextReminderDate())
                .status(Loan.LoanStatus.ACTIVE) // Default status for new loans
                .paidAmount(BigDecimal.ZERO) // Default paid amount
                // Note: Account will be set separately in the service
                // Note: totalAmount and remainingAmount are calculated in the service
                .build();
    }
    
    /**
     * Convert LoanUpdateRequestDto to Loan entity.
     *
     * @param requestDto the update request DTO
     * @return Loan entity
     */
    public Loan toEntityFromUpdateRequest(LoanUpdateRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        
        return Loan.builder()
                .personName(requestDto.getPersonName())
                .phoneNumber(requestDto.getPhoneNumber())
                .email(requestDto.getEmail())
                .loanType(requestDto.getLoanType())
                .principalAmount(requestDto.getPrincipalAmount())
                .interestRate(requestDto.getInterestRate())
                .loanDate(requestDto.getLoanDate())
                .dueDate(requestDto.getDueDate())
                .status(requestDto.getStatus())
                .description(requestDto.getDescription())
                .notes(requestDto.getNotes())
                .isUrgent(requestDto.getIsUrgent())
                .reminderEnabled(requestDto.getReminderEnabled())
                .nextReminderDate(requestDto.getNextReminderDate())
                // Note: Account will be set separately in the service
                // Note: Payment amounts should not be updated here
                .build();
    }
    
    /**
     * Update existing Loan entity from LoanUpdateRequestDto.
     *
     * @param entity the existing entity
     * @param requestDto the update request DTO
     */
    public void updateEntityFromUpdateRequest(Loan entity, LoanUpdateRequestDto requestDto) {
        if (entity == null || requestDto == null) {
            return;
        }
        
        entity.setPersonName(requestDto.getPersonName());
        entity.setPhoneNumber(requestDto.getPhoneNumber());
        entity.setEmail(requestDto.getEmail());
        entity.setLoanType(requestDto.getLoanType());
        entity.setPrincipalAmount(requestDto.getPrincipalAmount());
        entity.setInterestRate(requestDto.getInterestRate());
        entity.setLoanDate(requestDto.getLoanDate());
        entity.setDueDate(requestDto.getDueDate());
        entity.setStatus(requestDto.getStatus());
        entity.setDescription(requestDto.getDescription());
        entity.setNotes(requestDto.getNotes());
        entity.setIsUrgent(requestDto.getIsUrgent());
        entity.setReminderEnabled(requestDto.getReminderEnabled());
        entity.setNextReminderDate(requestDto.getNextReminderDate());
        // Note: Account will be set separately in the service
        // Note: Payment amounts and computed fields are not updated here
    }
    
    /**
     * Convert Loan entity to LoanResponseDto.
     *
     * @param loan the loan entity
     * @return LoanResponseDto
     */
    public LoanResponseDto toResponseDto(Loan loan) {
        return LoanResponseDto.fromEntity(loan);
    }
}
