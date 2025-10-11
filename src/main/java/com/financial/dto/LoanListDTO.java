package com.financial.dto;

import com.financial.entity.Loan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for loan list views.
 * Contains only essential fields to reduce data transfer and avoid N+1 queries.
 */
@Data
@Builder
public class LoanListDTO {
    
    private Long id;
    private String personName;
    private Loan.LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private Loan.LoanStatus status;
    private Boolean isUrgent;
    
    // Minimal account information
    private Long accountId;
    private String accountName;
    
    /**
     * Factory method to create LoanListDTO from Loan entity.
     *
     * @param loan the loan entity
     * @return LoanListDTO
     */
    public static LoanListDTO fromEntity(Loan loan) {
        return LoanListDTO.builder()
                .id(loan.getId())
                .personName(loan.getPersonName())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .totalAmount(loan.getTotalAmount())
                .paidAmount(loan.getPaidAmount())
                .remainingAmount(loan.getRemainingAmount())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .isUrgent(loan.getIsUrgent())
                .accountId(loan.getAccount() != null ? loan.getAccount().getId() : null)
                .accountName(loan.getAccount() != null ? loan.getAccount().getName() : null)
                .build();
    }
}

