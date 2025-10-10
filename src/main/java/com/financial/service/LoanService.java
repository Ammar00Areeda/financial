package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for loan-related business logic and reporting.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {
    
    private final LoanRepository loanRepository;
    
    /**
     * Get all loans with pagination.
     *
     * @param pageable pagination information
     * @return page of loans
     */
    @Transactional(readOnly = true)
    public Page<Loan> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }
    
    /**
     * Get all loans.
     *
     * @return list of all loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
    
    /**
     * Get loans by type (LENT or BORROWED).
     *
     * @param loanType the loan type
     * @return list of loans with the specified type
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByType(Loan.LoanType loanType) {
        return loanRepository.findByLoanType(loanType);
    }
    
    /**
     * Get loans by type with pagination.
     *
     * @param loanType the loan type
     * @param pageable pagination information
     * @return page of loans with the specified type
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByType(Loan.LoanType loanType, Pageable pageable) {
        return loanRepository.findByLoanType(loanType, pageable);
    }
    
    /**
     * Get loans by status.
     *
     * @param status the loan status
     * @return list of loans with the specified status
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }
    
    /**
     * Get loans by status with pagination.
     *
     * @param status the loan status
     * @param pageable pagination information
     * @return page of loans with the specified status
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByStatus(Loan.LoanStatus status, Pageable pageable) {
        return loanRepository.findByStatus(status, pageable);
    }
    
    /**
     * Get loans by type and status.
     *
     * @param loanType the loan type
     * @param status the loan status
     * @return list of loans with the specified type and status
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByTypeAndStatus(Loan.LoanType loanType, Loan.LoanStatus status) {
        return loanRepository.findByLoanTypeAndStatus(loanType, status);
    }
    
    /**
     * Get loans by account.
     *
     * @param account the account
     * @return list of loans for the account
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByAccount(Account account) {
        return loanRepository.findByAccount(account);
    }
    
    /**
     * Get loans by account and type.
     *
     * @param account the account
     * @param loanType the loan type
     * @return list of loans for the account and type
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByAccountAndType(Account account, Loan.LoanType loanType) {
        return loanRepository.findByAccountAndLoanType(account, loanType);
    }
    
    /**
     * Search loans by person name.
     *
     * @param personName the person name pattern to search for
     * @return list of loans matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Loan> searchLoansByPersonName(String personName) {
        return loanRepository.findByPersonNameContainingIgnoreCase(personName);
    }
    
    /**
     * Search loans by person name and type.
     *
     * @param personName the person name pattern to search for
     * @param loanType the loan type
     * @return list of loans matching the pattern and type
     */
    @Transactional(readOnly = true)
    public List<Loan> searchLoansByPersonNameAndType(String personName, Loan.LoanType loanType) {
        return loanRepository.findByPersonNameContainingIgnoreCaseAndLoanType(personName, loanType);
    }
    
    /**
     * Get loans by date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans within the date range
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return loanRepository.findByLoanDateBetween(startDate, endDate);
    }
    
    /**
     * Get loans by due date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans with due dates within the range
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByDueDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return loanRepository.findByDueDateBetween(startDate, endDate);
    }
    
    /**
     * Get overdue loans.
     *
     * @return list of overdue loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans() {
        return loanRepository.findOverdueLoans(LocalDateTime.now());
    }
    
    /**
     * Get loans due soon (within specified days).
     *
     * @param daysAhead the number of days ahead to check
     * @return list of loans due soon
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueSoon(int daysAhead) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime futureDate = currentDate.plusDays(daysAhead);
        return loanRepository.findLoansDueSoon(currentDate, futureDate);
    }
    
    /**
     * Get urgent loans.
     *
     * @return list of urgent loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getUrgentLoans() {
        return loanRepository.findByIsUrgent(true);
    }
    
    /**
     * Get loans with reminders due.
     *
     * @return list of loans with reminders due
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansWithRemindersDue() {
        return loanRepository.findLoansWithRemindersDue(LocalDateTime.now());
    }
    
    /**
     * Get loan by ID.
     *
     * @param id the loan ID
     * @return Optional containing the loan if found
     */
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }
    
    /**
     * Create a new loan.
     *
     * @param loan the loan to create
     * @return the created loan
     */
    public Loan createLoan(Loan loan) {
        // Calculate total amount if interest rate is provided
        if (loan.getInterestRate() != null && loan.getInterestRate().compareTo(BigDecimal.ZERO) > 0) {
            // Simple interest calculation: Principal * (1 + (Rate * Time))
            // For now, assuming 1 year time period
            BigDecimal interest = loan.getPrincipalAmount().multiply(loan.getInterestRate()).divide(BigDecimal.valueOf(100));
            loan.setTotalAmount(loan.getPrincipalAmount().add(interest));
        } else {
            loan.setTotalAmount(loan.getPrincipalAmount());
        }
        
        // Set remaining amount
        loan.setRemainingAmount(loan.getTotalAmount());
        
        return loanRepository.save(loan);
    }
    
    /**
     * Update an existing loan.
     *
     * @param loan the loan to update
     * @return the updated loan
     */
    public Loan updateLoan(Loan loan) {
        return loanRepository.save(loan);
    }
    
    /**
     * Delete loan by ID.
     *
     * @param id the loan ID
     * @throws IllegalArgumentException if loan not found
     */
    public void deleteLoan(Long id) {
        if (!loanRepository.existsById(id)) {
            throw new IllegalArgumentException("Loan with ID " + id + " not found");
        }
        
        loanRepository.deleteById(id);
    }
    
    /**
     * Record a payment for a loan.
     *
     * @param loanId the loan ID
     * @param paymentAmount the payment amount
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found
     */
    public Loan recordPayment(Long loanId, BigDecimal paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        BigDecimal newPaidAmount = loan.getPaidAmount().add(paymentAmount);
        loan.setPaidAmount(newPaidAmount);
        loan.setLastPaymentDate(LocalDateTime.now());
        
        // Update remaining amount
        BigDecimal newRemainingAmount = loan.getTotalAmount().subtract(newPaidAmount);
        loan.setRemainingAmount(newRemainingAmount);
        
        // Update status based on remaining amount
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(Loan.LoanStatus.PAID_OFF);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            loan.setStatus(Loan.LoanStatus.PARTIALLY_PAID);
        }
        
        return loanRepository.save(loan);
    }
    
    /**
     * Mark loan as urgent.
     *
     * @param loanId the loan ID
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found
     */
    public Loan markAsUrgent(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        loan.setIsUrgent(true);
        return loanRepository.save(loan);
    }
    
    /**
     * Mark loan as not urgent.
     *
     * @param loanId the loan ID
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found
     */
    public Loan markAsNotUrgent(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        loan.setIsUrgent(false);
        return loanRepository.save(loan);
    }
    
    // ========== REPORTING METHODS ==========
    
    /**
     * Get loan summary report.
     *
     * @return LoanSummaryReport containing key metrics
     */
    @Transactional(readOnly = true)
    public LoanSummaryReport getLoanSummaryReport() {
        BigDecimal totalLent = loanRepository.calculateTotalAmountLent();
        BigDecimal totalBorrowed = loanRepository.calculateTotalAmountBorrowed();
        BigDecimal totalRepaidForLent = loanRepository.calculateTotalAmountRepaidForLent();
        BigDecimal totalRepaidForBorrowed = loanRepository.calculateTotalAmountRepaidForBorrowed();
        BigDecimal netPosition = loanRepository.calculateNetLoanPosition();
        
        List<Loan> activeLentLoans = loanRepository.findByLoanTypeAndStatus(Loan.LoanType.LENT, Loan.LoanStatus.ACTIVE);
        List<Loan> activeBorrowedLoans = loanRepository.findByLoanTypeAndStatus(Loan.LoanType.BORROWED, Loan.LoanStatus.ACTIVE);
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDateTime.now());
        
        return LoanSummaryReport.builder()
                .totalAmountLent(totalLent)
                .totalAmountBorrowed(totalBorrowed)
                .totalRepaidForLent(totalRepaidForLent)
                .totalRepaidForBorrowed(totalRepaidForBorrowed)
                .netLoanPosition(netPosition)
                .activeLentLoansCount(activeLentLoans.size())
                .activeBorrowedLoansCount(activeBorrowedLoans.size())
                .overdueLoansCount(overdueLoans.size())
                .build();
    }
    
    /**
     * Get loans by person report.
     *
     * @return list of PersonLoanSummary for each person
     */
    @Transactional(readOnly = true)
    public List<PersonLoanSummary> getLoansByPersonReport() {
        // This would require a custom query to group by person name
        // For now, return all loans and group them in the controller
        return List.of(); // Placeholder - would need custom implementation
    }
    
    /**
     * Get overdue loans report.
     *
     * @return list of overdue loans with details
     */
    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoansReport() {
        return loanRepository.findOverdueLoans(LocalDateTime.now());
    }
    
    /**
     * Get loans due soon report.
     *
     * @param daysAhead the number of days ahead to check
     * @return list of loans due soon
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueSoonReport(int daysAhead) {
        return getLoansDueSoon(daysAhead);
    }
    
    /**
     * Get total amount lent.
     *
     * @return total amount lent
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountLent() {
        return loanRepository.calculateTotalAmountLent();
    }
    
    /**
     * Get total amount borrowed.
     *
     * @return total amount borrowed
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountBorrowed() {
        return loanRepository.calculateTotalAmountBorrowed();
    }
    
    /**
     * Get net loan position (lent - borrowed).
     *
     * @return net loan position
     */
    @Transactional(readOnly = true)
    public BigDecimal getNetLoanPosition() {
        return loanRepository.calculateNetLoanPosition();
    }
    
    /**
     * Get total amount lent by status.
     *
     * @param status the loan status
     * @return total amount lent with the specified status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountLentByStatus(Loan.LoanStatus status) {
        return loanRepository.calculateTotalAmountLentByStatus(status);
    }
    
    /**
     * Get total amount borrowed by status.
     *
     * @param status the loan status
     * @return total amount borrowed with the specified status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountBorrowedByStatus(Loan.LoanStatus status) {
        return loanRepository.calculateTotalAmountBorrowedByStatus(status);
    }
    
    // ========== INNER CLASSES FOR REPORTS ==========
    
    /**
     * Summary report for loans.
     */
    @lombok.Data
    @lombok.Builder
    public static class LoanSummaryReport {
        private BigDecimal totalAmountLent;
        private BigDecimal totalAmountBorrowed;
        private BigDecimal totalRepaidForLent;
        private BigDecimal totalRepaidForBorrowed;
        private BigDecimal netLoanPosition;
        private int activeLentLoansCount;
        private int activeBorrowedLoansCount;
        private int overdueLoansCount;
    }
    
    /**
     * Summary report for loans by person.
     */
    @lombok.Data
    @lombok.Builder
    public static class PersonLoanSummary {
        private String personName;
        private BigDecimal totalLent;
        private BigDecimal totalBorrowed;
        private BigDecimal totalRepaid;
        private int activeLoansCount;
        private int overdueLoansCount;
    }
}
