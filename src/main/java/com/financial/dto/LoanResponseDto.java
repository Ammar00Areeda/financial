package com.financial.dto;

import com.financial.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for loan responses (GET requests).
 * Contains all loan information including computed fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponseDto {
    
    private Long id;
    private String personName;
    private String phoneNumber;
    private String email;
    private Loan.LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LocalDateTime lastPaymentDate;
    private Loan.LoanStatus status;
    private String description;
    private String notes;
    private Boolean isUrgent;
    private Boolean reminderEnabled;
    private LocalDateTime nextReminderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Account information
    private Long accountId;
    private String accountName;
    private String accountType;
    
    // Computed fields
    private BigDecimal percentagePaid;
    private Boolean isOverdue;
    private Boolean isFullyPaid;
    
    /**
     * Factory method to create LoanResponseDto from Loan entity.
     *
     * @param loan the loan entity
     * @return LoanResponseDto
     */
    public static LoanResponseDto fromEntity(Loan loan) {
        return LoanResponseDto.builder()
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
                .isUrgent(loan.getIsUrgent())
                .reminderEnabled(loan.getReminderEnabled())
                .nextReminderDate(loan.getNextReminderDate())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null)
                .accountName(loan.getAccount() != null ? loan.getAccount().getName() : null)
                .accountType(loan.getAccount() != null ? loan.getAccount().getType().name() : null)
                .percentagePaid(loan.getPercentagePaid())
                .isOverdue(loan.isOverdue())
                .isFullyPaid(loan.isFullyPaid())
                .build();
    }
}
