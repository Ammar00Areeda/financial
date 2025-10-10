package com.financial.repository;

import com.financial.entity.Account;
import com.financial.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Loan entity operations.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    /**
     * Find loans by type (LENT or BORROWED).
     *
     * @param loanType the loan type
     * @return list of loans with the specified type
     */
    List<Loan> findByLoanType(Loan.LoanType loanType);
    
    /**
     * Find loans by status.
     *
     * @param status the loan status
     * @return list of loans with the specified status
     */
    List<Loan> findByStatus(Loan.LoanStatus status);
    
    /**
     * Find loans by type and status.
     *
     * @param loanType the loan type
     * @param status the loan status
     * @return list of loans with the specified type and status
     */
    List<Loan> findByLoanTypeAndStatus(Loan.LoanType loanType, Loan.LoanStatus status);
    
    /**
     * Find loans by person name containing (case-insensitive).
     *
     * @param personName the person name pattern to search for
     * @return list of loans matching the pattern
     */
    @Query("SELECT l FROM Loan l WHERE LOWER(l.personName) LIKE LOWER(CONCAT('%', :personName, '%'))")
    List<Loan> findByPersonNameContainingIgnoreCase(@Param("personName") String personName);
    
    /**
     * Find loans by person name containing and type (case-insensitive).
     *
     * @param personName the person name pattern to search for
     * @param loanType the loan type
     * @return list of loans matching the pattern and type
     */
    @Query("SELECT l FROM Loan l WHERE LOWER(l.personName) LIKE LOWER(CONCAT('%', :personName, '%')) AND l.loanType = :loanType")
    List<Loan> findByPersonNameContainingIgnoreCaseAndLoanType(@Param("personName") String personName, @Param("loanType") Loan.LoanType loanType);
    
    /**
     * Find loans by account.
     *
     * @param account the account
     * @return list of loans for the account
     */
    List<Loan> findByAccount(Account account);
    
    /**
     * Find loans by account and type.
     *
     * @param account the account
     * @param loanType the loan type
     * @return list of loans for the account and type
     */
    List<Loan> findByAccountAndLoanType(Account account, Loan.LoanType loanType);
    
    /**
     * Find loans by date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans within the date range
     */
    List<Loan> findByLoanDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find loans by due date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of loans with due dates within the range
     */
    List<Loan> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find overdue loans.
     *
     * @param currentDate the current date
     * @return list of overdue loans
     */
    @Query("SELECT l FROM Loan l WHERE l.dueDate < :currentDate AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Find loans due soon (within specified days).
     *
     * @param currentDate the current date
     * @param daysAhead the number of days ahead to check
     * @return list of loans due soon
     */
    @Query("SELECT l FROM Loan l WHERE l.dueDate BETWEEN :currentDate AND :futureDate AND l.status = 'ACTIVE'")
    List<Loan> findLoansDueSoon(@Param("currentDate") LocalDateTime currentDate, @Param("futureDate") LocalDateTime futureDate);
    
    /**
     * Find urgent loans.
     *
     * @param isUrgent whether the loan is urgent
     * @return list of urgent loans
     */
    List<Loan> findByIsUrgent(Boolean isUrgent);
    
    /**
     * Find loans with reminders enabled.
     *
     * @param reminderEnabled whether reminders are enabled
     * @return list of loans with reminders enabled
     */
    List<Loan> findByReminderEnabled(Boolean reminderEnabled);
    
    /**
     * Find loans with reminders due.
     *
     * @param currentDate the current date
     * @return list of loans with reminders due
     */
    @Query("SELECT l FROM Loan l WHERE l.reminderEnabled = true AND l.nextReminderDate <= :currentDate")
    List<Loan> findLoansWithRemindersDue(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Calculate total amount lent (money you lent to others).
     *
     * @return total amount lent
     */
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l WHERE l.loanType = 'LENT'")
    BigDecimal calculateTotalAmountLent();
    
    /**
     * Calculate total amount borrowed (money you borrowed from others).
     *
     * @return total amount borrowed
     */
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l WHERE l.loanType = 'BORROWED'")
    BigDecimal calculateTotalAmountBorrowed();
    
    /**
     * Calculate total amount lent by status.
     *
     * @param status the loan status
     * @return total amount lent with the specified status
     */
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l WHERE l.loanType = 'LENT' AND l.status = :status")
    BigDecimal calculateTotalAmountLentByStatus(@Param("status") Loan.LoanStatus status);
    
    /**
     * Calculate total amount borrowed by status.
     *
     * @param status the loan status
     * @return total amount borrowed with the specified status
     */
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l WHERE l.loanType = 'BORROWED' AND l.status = :status")
    BigDecimal calculateTotalAmountBorrowedByStatus(@Param("status") Loan.LoanStatus status);
    
    /**
     * Calculate total amount repaid for lent money.
     *
     * @return total amount repaid for lent money
     */
    @Query("SELECT COALESCE(SUM(l.paidAmount), 0) FROM Loan l WHERE l.loanType = 'LENT'")
    BigDecimal calculateTotalAmountRepaidForLent();
    
    /**
     * Calculate total amount repaid for borrowed money.
     *
     * @return total amount repaid for borrowed money
     */
    @Query("SELECT COALESCE(SUM(l.paidAmount), 0) FROM Loan l WHERE l.loanType = 'BORROWED'")
    BigDecimal calculateTotalAmountRepaidForBorrowed();
    
    /**
     * Calculate net loan position (lent - borrowed).
     *
     * @return net loan position
     */
    @Query("SELECT (SELECT COALESCE(SUM(l1.principalAmount), 0) FROM Loan l1 WHERE l1.loanType = 'LENT') - " +
           "(SELECT COALESCE(SUM(l2.principalAmount), 0) FROM Loan l2 WHERE l2.loanType = 'BORROWED')")
    BigDecimal calculateNetLoanPosition();
    
    /**
     * Find loans by amount range.
     *
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return list of loans within the amount range
     */
    List<Loan> findByPrincipalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Find loans by person name and type.
     *
     * @param personName the person name
     * @param loanType the loan type
     * @return list of loans for the person and type
     */
    List<Loan> findByPersonNameAndLoanType(String personName, Loan.LoanType loanType);
    
    /**
     * Find all loans with pagination.
     *
     * @param pageable pagination information
     * @return page of loans
     */
    Page<Loan> findAll(Pageable pageable);
    
    /**
     * Find loans by type with pagination.
     *
     * @param loanType the loan type
     * @param pageable pagination information
     * @return page of loans with the specified type
     */
    Page<Loan> findByLoanType(Loan.LoanType loanType, Pageable pageable);
    
    /**
     * Find loans by status with pagination.
     *
     * @param status the loan status
     * @param pageable pagination information
     * @return page of loans with the specified status
     */
    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
}
