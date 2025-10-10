package com.financial.service;

import com.financial.entity.Account;
import com.financial.entity.Loan;
import com.financial.entity.User;
import com.financial.repository.LoanRepository;
import com.financial.security.SecurityUtils;
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
     * Get all loans for the authenticated user with pagination.
     *
     * @param pageable pagination information
     * @return page of loans
     */
    @Transactional(readOnly = true)
    public Page<Loan> getAllLoans(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUser(currentUser, pageable);
    }
    
    /**
     * Get all loans for the authenticated user.
     *
     * @return list of all loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
    }
    
    /**
     * Get loans by type (LENT or BORROWED) for the authenticated user.
     *
     * @param loanType the loan type
     * @return list of loans with the specified type
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByType(Loan.LoanType loanType) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUserAndLoanType(currentUser, loanType);
    }
    
    /**
     * Get loans by type with pagination for the authenticated user.
     *
     * @param loanType the loan type
     * @param pageable pagination information
     * @return page of loans with the specified type
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByType(Loan.LoanType loanType, Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        Page<Loan> allLoans = loanRepository.findByUser(currentUser, pageable);
        return allLoans; // Note: This should ideally filter by type in repository
    }
    
    /**
     * Get loans by status for the authenticated user.
     *
     * @param status the loan status
     * @return list of loans with the specified status
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUserAndStatus(currentUser, status);
    }
    
    /**
     * Get loans by status with pagination for the authenticated user.
     *
     * @param status the loan status
     * @param pageable pagination information
     * @return page of loans with the specified status
     */
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByStatus(Loan.LoanStatus status, Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUser(currentUser, pageable); // Note: Should filter by status
    }
    
    /**
     * Get loans by type and status for the authenticated user.
     *
     * @param loanType the loan type
     * @param status the loan status
     * @return list of loans with the specified type and status
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByTypeAndStatus(Loan.LoanType loanType, Loan.LoanStatus status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> userLoans = loanRepository.findByUserAndLoanType(currentUser, loanType);
        return userLoans.stream().filter(l -> l.getStatus() == status).toList();
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
     * Search loans by person name for the authenticated user.
     *
     * @param personName the person name pattern to search for
     * @return list of loans matching the pattern
     */
    @Transactional(readOnly = true)
    public List<Loan> searchLoansByPersonName(String personName) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> l.getPersonName() != null && 
                            l.getPersonName().toLowerCase().contains(personName.toLowerCase()))
                .toList();
    }
    
    /**
     * Search loans by person name and type for the authenticated user.
     *
     * @param personName the person name pattern to search for
     * @param loanType the loan type
     * @return list of loans matching the pattern and type
     */
    @Transactional(readOnly = true)
    public List<Loan> searchLoansByPersonNameAndType(String personName, Loan.LoanType loanType) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> userLoans = loanRepository.findByUserAndLoanType(currentUser, loanType);
        return userLoans.stream()
                .filter(l -> l.getPersonName() != null && 
                            l.getPersonName().toLowerCase().contains(personName.toLowerCase()))
                .toList();
    }
    
    /**
     * Get loans by date range for the authenticated user.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans within the date range
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> !l.getLoanDate().isBefore(startDate) && !l.getLoanDate().isAfter(endDate))
                .toList();
    }
    
    /**
     * Get loans by due date range for the authenticated user.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans with due dates within the range
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansByDueDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> l.getDueDate() != null && 
                            !l.getDueDate().isBefore(startDate) && !l.getDueDate().isAfter(endDate))
                .toList();
    }
    
    /**
     * Get overdue loans for the authenticated user.
     *
     * @return list of overdue loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoans() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findOverdueLoansByUser(currentUser, LocalDateTime.now());
    }
    
    /**
     * Get loans due soon (within specified days) for the authenticated user.
     *
     * @param daysAhead the number of days ahead to check
     * @return list of loans due soon
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueSoon(int daysAhead) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime futureDate = currentDate.plusDays(daysAhead);
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> l.getDueDate() != null && 
                            !l.getDueDate().isBefore(currentDate) && 
                            !l.getDueDate().isAfter(futureDate) &&
                            l.getStatus() == Loan.LoanStatus.ACTIVE)
                .toList();
    }
    
    /**
     * Get urgent loans for the authenticated user.
     *
     * @return list of urgent loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getUrgentLoans() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> l.getIsUrgent() != null && l.getIsUrgent())
                .toList();
    }
    
    /**
     * Get loans with reminders due for the authenticated user.
     *
     * @return list of loans with reminders due
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansWithRemindersDue() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        LocalDateTime now = LocalDateTime.now();
        List<Loan> allLoans = loanRepository.findByUser(currentUser, Pageable.unpaged()).getContent();
        return allLoans.stream()
                .filter(l -> l.getReminderEnabled() != null && 
                            l.getReminderEnabled() && 
                            l.getNextReminderDate() != null &&
                            !l.getNextReminderDate().isAfter(now))
                .toList();
    }
    
    /**
     * Get loan by ID for the authenticated user.
     *
     * @param id the loan ID
     * @return Optional containing the loan if found
     */
    @Transactional(readOnly = true)
    public Optional<Loan> getLoanById(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByIdAndUser(id, currentUser);
    }
    
    /**
     * Create a new loan for the authenticated user.
     *
     * @param loan the loan to create
     * @return the created loan
     */
    public Loan createLoan(Loan loan) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Associate loan with current user
        loan.setUser(currentUser);
        
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
     * Update an existing loan for the authenticated user.
     *
     * @param loan the loan to update
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found or doesn't belong to user
     */
    public Loan updateLoan(Loan loan) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Verify loan belongs to current user
        Loan existingLoan = loanRepository.findByIdAndUser(loan.getId(), currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loan.getId() + " not found"));
        
        // Ensure user association is not changed
        loan.setUser(currentUser);
        
        return loanRepository.save(loan);
    }
    
    /**
     * Delete loan by ID for the authenticated user.
     *
     * @param id the loan ID
     * @throws IllegalArgumentException if loan not found or doesn't belong to user
     */
    public void deleteLoan(Long id) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Loan loan = loanRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + id + " not found"));
        
        loanRepository.delete(loan);
    }
    
    /**
     * Record a payment for a loan belonging to the authenticated user.
     *
     * @param loanId the loan ID
     * @param paymentAmount the payment amount
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found or doesn't belong to user
     */
    public Loan recordPayment(Long loanId, BigDecimal paymentAmount) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Loan loan = loanRepository.findByIdAndUser(loanId, currentUser)
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
     * Mark loan as urgent for the authenticated user.
     *
     * @param loanId the loan ID
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found or doesn't belong to user
     */
    public Loan markAsUrgent(Long loanId) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Loan loan = loanRepository.findByIdAndUser(loanId, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        loan.setIsUrgent(true);
        return loanRepository.save(loan);
    }
    
    /**
     * Mark loan as not urgent for the authenticated user.
     *
     * @param loanId the loan ID
     * @return the updated loan
     * @throws IllegalArgumentException if loan not found or doesn't belong to user
     */
    public Loan markAsNotUrgent(Long loanId) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        Loan loan = loanRepository.findByIdAndUser(loanId, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        loan.setIsUrgent(false);
        return loanRepository.save(loan);
    }
    
    // ========== REPORTING METHODS ==========
    
    /**
     * Get loan summary report for the authenticated user.
     *
     * @return LoanSummaryReport containing key metrics
     */
    @Transactional(readOnly = true)
    public LoanSummaryReport getLoanSummaryReport() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        BigDecimal totalLent = loanRepository.calculateTotalAmountLentByUser(currentUser);
        BigDecimal totalBorrowed = loanRepository.calculateTotalAmountBorrowedByUser(currentUser);
        
        // Calculate total repaid amounts
        List<Loan> lentLoans = loanRepository.findByUserAndLoanType(currentUser, Loan.LoanType.LENT);
        List<Loan> borrowedLoans = loanRepository.findByUserAndLoanType(currentUser, Loan.LoanType.BORROWED);
        
        BigDecimal totalRepaidForLent = lentLoans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRepaidForBorrowed = borrowedLoans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netPosition = totalLent.subtract(totalBorrowed);
        
        List<Loan> activeLentLoans = lentLoans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .toList();
        List<Loan> activeBorrowedLoans = borrowedLoans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
                .toList();
        List<Loan> overdueLoans = loanRepository.findOverdueLoansByUser(currentUser, LocalDateTime.now());
        
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
     * Get overdue loans report for the authenticated user.
     *
     * @return list of overdue loans with details
     */
    @Transactional(readOnly = true)
    public List<Loan> getOverdueLoansReport() {
        return getOverdueLoans();
    }
    
    /**
     * Get loans due soon report for the authenticated user.
     *
     * @param daysAhead the number of days ahead to check
     * @return list of loans due soon
     */
    @Transactional(readOnly = true)
    public List<Loan> getLoansDueSoonReport(int daysAhead) {
        return getLoansDueSoon(daysAhead);
    }
    
    /**
     * Get total amount lent for the authenticated user.
     *
     * @return total amount lent
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountLent() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.calculateTotalAmountLentByUser(currentUser);
    }
    
    /**
     * Get total amount borrowed for the authenticated user.
     *
     * @return total amount borrowed
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountBorrowed() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.calculateTotalAmountBorrowedByUser(currentUser);
    }
    
    /**
     * Get net loan position (lent - borrowed) for the authenticated user.
     *
     * @return net loan position
     */
    @Transactional(readOnly = true)
    public BigDecimal getNetLoanPosition() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        BigDecimal totalLent = loanRepository.calculateTotalAmountLentByUser(currentUser);
        BigDecimal totalBorrowed = loanRepository.calculateTotalAmountBorrowedByUser(currentUser);
        return totalLent.subtract(totalBorrowed);
    }
    
    /**
     * Get total amount lent by status for the authenticated user.
     *
     * @param status the loan status
     * @return total amount lent with the specified status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountLentByStatus(Loan.LoanStatus status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> loans = loanRepository.findByUserAndLoanType(currentUser, Loan.LoanType.LENT);
        return loans.stream()
                .filter(l -> l.getStatus() == status)
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total amount borrowed by status for the authenticated user.
     *
     * @param status the loan status
     * @return total amount borrowed with the specified status
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountBorrowedByStatus(Loan.LoanStatus status) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        List<Loan> loans = loanRepository.findByUserAndLoanType(currentUser, Loan.LoanType.BORROWED);
        return loans.stream()
                .filter(l -> l.getStatus() == status)
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
