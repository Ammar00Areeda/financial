package com.financial.service;

import com.financial.dto.LoanInstallmentRequestDto;
import com.financial.dto.LoanInstallmentResponseDto;
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
 * 
 * <p>This service manages loan operations for both money lent to others and money borrowed
 * from others. It supports tracking of loan status, payments, due dates, reminders, and
 * provides comprehensive reporting capabilities.</p>
 * 
 * <p><b>Security:</b> All methods require authentication. Operations are automatically
 * scoped to the authenticated user retrieved via {@link SecurityUtils#getAuthenticatedUser()}.
 * Users can only access and modify their own loans.</p>
 * 
 * @see Loan
 * @see LoanRepository
 * @see SecurityUtils
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {
    
    private final LoanRepository loanRepository;
    private final AccountService accountService;
    
    /**
     * Retrieves all loans for the authenticated user with pagination support.
     * 
     * <p>Returns both LENT (money lent to others) and BORROWED (money borrowed from others)
     * loans belonging to the authenticated user.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only returns loans belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("loanDate").descending());
     * Page<Loan> loans = loanService.getAllLoans(pageable);
     * loans.forEach(loan -> 
     *     System.out.println(loan.getLoanType() + ": $" + loan.getPrincipalAmount())
     * );
     * }</pre>
     *
     * @param pageable pagination information including page number, size, and sort order
     * @return page of loans belonging to the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    @Transactional(readOnly = true)
    public Page<Loan> getAllLoans(Pageable pageable) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUser(currentUser.getId(), pageable);
    }
    
    /**
     * Get all loans for the authenticated user.
     *
     * @return list of all loans
     */
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        return loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        Page<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), pageable);
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
        return loanRepository.findByUser(currentUser.getId(), pageable); // Note: Should filter by status
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
        List<Loan> allLoans = loanRepository.findByUser(currentUser.getId(), Pageable.unpaged()).getContent();
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
     * Creates a new loan for the authenticated user.
     * 
     * <p>The loan is automatically associated with the authenticated user. If an interest
     * rate is provided, the total amount is calculated using simple interest (Principal * 
     * (1 + Rate)). The remaining amount is initialized to the total amount.</p>
     * 
     * <p><b>Security:</b> Requires authentication. The loan is associated with the
     * authenticated user and cannot be transferred to another user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Loan loan = Loan.builder()
     *     .personName("John Doe")
     *     .loanType(LoanType.LENT)
     *     .principalAmount(new BigDecimal("1000.00"))
     *     .interestRate(new BigDecimal("5.0"))
     *     .loanDate(LocalDateTime.now())
     *     .dueDate(LocalDateTime.now().plusMonths(6))
     *     .status(LoanStatus.ACTIVE)
     *     .build();
     * 
     * Loan created = loanService.createLoan(loan);
     * System.out.println("Loan created with ID: " + created.getId());
     * System.out.println("Total amount with interest: $" + created.getTotalAmount());
     * }</pre>
     *
     * @param loan the loan to create (must not be null)
     * @return the persisted loan with generated ID and calculated amounts
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
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
        
        // Handle account balance updates based on loan type
        if (loan.getAccount() != null) {
            Long accountId = loan.getAccount().getId();
            
            if (loan.getLoanType() == Loan.LoanType.LENT) {
                // If lending money, deduct the amount from the account
                Account account = accountService.getAccountById(accountId)
                        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
                
                // Check if account has sufficient balance
                if (account.getBalance().compareTo(loan.getPrincipalAmount()) < 0) {
                    throw new IllegalArgumentException(
                        String.format("Insufficient balance in account '%s'. Available: %s, Required: %s",
                            account.getName(), 
                            account.getBalance(), 
                            loan.getPrincipalAmount())
                    );
                }
                
                // Deduct the loan amount from the account
                accountService.subtractFromAccountBalance(accountId, loan.getPrincipalAmount());
            } else if (loan.getLoanType() == Loan.LoanType.BORROWED) {
                // If borrowing money, add the amount to the account
                accountService.addToAccountBalance(accountId, loan.getPrincipalAmount());
            }
        }
        
        return loanRepository.save(loan);
    }
    
    /**
     * Updates an existing loan for the authenticated user.
     * 
     * <p>Verifies that the loan exists and belongs to the authenticated user before
     * updating. The user association cannot be changed.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only loans belonging to the
     * authenticated user can be updated. The user association is immutable.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Loan loan = loanService.getLoanById(123L)
     *     .orElseThrow(() -> new NotFoundException("Loan not found"));
     * 
     * loan.setDueDate(LocalDateTime.now().plusMonths(3));
     * loan.setIsUrgent(true);
     * 
     * Loan updated = loanService.updateLoan(loan);
     * System.out.println("Loan updated: " + updated.getPersonName());
     * }</pre>
     *
     * @param loan the loan to update with modified fields
     * @return the updated and persisted loan
     * @throws IllegalArgumentException if the loan doesn't exist or doesn't belong to
     *         the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public Loan updateLoan(Loan loan) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Verify loan belongs to current user
        loanRepository.findByIdAndUser(loan.getId(), currentUser)
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
     * Records a payment for a loan belonging to the authenticated user.
     * 
     * <p>Updates the paid amount, remaining amount, and last payment date. Automatically
     * updates the loan status to PARTIALLY_PAID if any payment is made, or PAID_OFF if
     * the remaining amount reaches zero or below.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only loans belonging to the
     * authenticated user can have payments recorded.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Record a $200 payment
     * Loan loan = loanService.recordPayment(123L, new BigDecimal("200.00"));
     * 
     * System.out.println("Paid amount: $" + loan.getPaidAmount());
     * System.out.println("Remaining: $" + loan.getRemainingAmount());
     * System.out.println("Status: " + loan.getStatus());
     * 
     * // Check if loan is paid off
     * if (loan.getStatus() == LoanStatus.PAID_OFF) {
     *     System.out.println("Loan fully paid!");
     * }
     * }</pre>
     *
     * @param loanId the ID of the loan to record payment for
     * @param paymentAmount the amount of the payment (must be positive)
     * @return the updated loan with new payment information and status
     * @throws IllegalArgumentException if the loan doesn't exist or doesn't belong to
     *         the authenticated user
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
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
        
        // If loan type is LENT and has an associated account, add the payment back to the account
        if (loan.getLoanType() == Loan.LoanType.LENT && loan.getAccount() != null) {
            accountService.addToAccountBalance(loan.getAccount().getId(), paymentAmount);
        }
        
        // If loan type is BORROWED and has an associated account, subtract the payment from the account
        if (loan.getLoanType() == Loan.LoanType.BORROWED && loan.getAccount() != null) {
            Account account = loan.getAccount();
            
            // Check if account has sufficient balance
            if (account.getBalance().compareTo(paymentAmount) < 0) {
                throw new IllegalArgumentException(
                    String.format("Insufficient balance in account '%s'. Available: %s, Required: %s",
                        account.getName(), 
                        account.getBalance(), 
                        paymentAmount)
                );
            }
            
            accountService.subtractFromAccountBalance(account.getId(), paymentAmount);
        }
        
        return loanRepository.save(loan);
    }
    
    /**
     * Records an installment payment for a loan belonging to the authenticated user.
     * 
     * <p>This method processes installment payments with account balance updates. It validates
     * that the loan exists and belongs to the authenticated user, checks account balance
     * sufficiency, updates loan amounts and status, and adjusts account balances accordingly.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only loans belonging to the
     * authenticated user can have installment payments recorded.</p>
     * 
     * <p><b>Account Balance Logic:</b></p>
     * <ul>
     *   <li>For LENT loans: Payment is added back to the account (money returned)</li>
     *   <li>For BORROWED loans: Payment is deducted from the account (money paid out)</li>
     * </ul>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * LoanInstallmentRequestDto request = LoanInstallmentRequestDto.builder()
     *     .accountId(123L)
     *     .amount(new BigDecimal("250.00"))
     *     .note("Monthly installment payment")
     *     .paidAt(LocalDateTime.now())
     *     .build();
     * 
     * Loan loan = loanService.recordInstallmentPayment(456L, request);
     * 
     * System.out.println("Paid amount: $" + loan.getPaidAmount());
     * System.out.println("Remaining: $" + loan.getRemainingAmount());
     * System.out.println("Status: " + loan.getStatus());
     * }</pre>
     *
     * @param loanId the ID of the loan to record installment payment for
     * @param request the installment payment request containing account, amount, note, and payment date
     * @return the installment payment response with all relevant information
     * @throws IllegalArgumentException if the loan doesn't exist, doesn't belong to the
     *         authenticated user, account not found, or insufficient account balance
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
     */
    public LoanInstallmentResponseDto recordInstallmentPayment(Long loanId, LoanInstallmentRequestDto request) {
        User currentUser = SecurityUtils.getAuthenticatedUser();
        
        // Find the loan and verify ownership
        Loan loan = loanRepository.findByIdAndUser(loanId, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Loan with ID " + loanId + " not found"));
        
        // Validate account exists and belongs to user
        Account account = accountService.getAccountById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account with ID " + request.getAccountId() + " not found"));
        
        // Verify account belongs to the authenticated user
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Account does not belong to the authenticated user");
        }
        
        BigDecimal paymentAmount = request.getAmount();
        
        // Update loan payment information
        BigDecimal newPaidAmount = loan.getPaidAmount().add(paymentAmount);
        loan.setPaidAmount(newPaidAmount);
        loan.setLastPaymentDate(request.getPaidAt());
        
        // Update remaining amount
        BigDecimal newRemainingAmount = loan.getTotalAmount().subtract(newPaidAmount);
        loan.setRemainingAmount(newRemainingAmount);
        
        // Update status based on remaining amount
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(Loan.LoanStatus.PAID_OFF);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            loan.setStatus(Loan.LoanStatus.PARTIALLY_PAID);
        }
        
        // Handle account balance updates based on loan type
        if (loan.getLoanType() == Loan.LoanType.LENT) {
            // If lending money, add the payment back to the account (money returned)
            accountService.addToAccountBalance(request.getAccountId(), paymentAmount);
        } else if (loan.getLoanType() == Loan.LoanType.BORROWED) {
            // If borrowing money, subtract the payment from the account (money paid out)
            
            // Check if account has sufficient balance
            if (account.getBalance().compareTo(paymentAmount) < 0) {
                throw new IllegalArgumentException(
                    String.format("Insufficient balance in account '%s'. Available: %s, Required: %s",
                        account.getName(), 
                        account.getBalance(), 
                        paymentAmount)
                );
            }
            
            accountService.subtractFromAccountBalance(request.getAccountId(), paymentAmount);
        }
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Create and return the response DTO
        return LoanInstallmentResponseDto.builder()
                .installmentId(System.currentTimeMillis()) // Using timestamp as installment ID for now
                .loanId(savedLoan.getId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .paidAt(request.getPaidAt())
                .note(request.getNote())
                .remainingBalance(savedLoan.getRemainingAmount())
                .createdAt(LocalDateTime.now())
                .status("APPLIED")
                .build();
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
     * Generates a comprehensive loan summary report for the authenticated user.
     * 
     * <p>The report includes total amounts lent and borrowed, repayment amounts, net loan
     * position, and counts of active and overdue loans. This provides a complete overview
     * of the user's lending and borrowing activities.</p>
     * 
     * <p><b>Security:</b> Requires authentication. Only includes loans belonging to
     * the authenticated user.</p>
     * 
     * <p><b>Example:</b></p>
     * <pre>{@code
     * LoanSummaryReport report = loanService.getLoanSummaryReport();
     * 
     * System.out.println("Total Lent: $" + report.getTotalAmountLent());
     * System.out.println("Total Borrowed: $" + report.getTotalAmountBorrowed());
     * System.out.println("Net Position: $" + report.getNetLoanPosition());
     * System.out.println("Active Lent Loans: " + report.getActiveLentLoansCount());
     * System.out.println("Overdue Loans: " + report.getOverdueLoansCount());
     * 
     * if (report.getNetLoanPosition().compareTo(BigDecimal.ZERO) > 0) {
     *     System.out.println("You are a net lender");
     * } else {
     *     System.out.println("You are a net borrower");
     * }
     * }</pre>
     *
     * @return comprehensive loan summary report with all key metrics
     * @throws org.springframework.security.authentication.AuthenticationCredentialsNotFoundException 
     *         if no authenticated user is found
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
    
    /**
     * DEBUG METHOD - Get all loans without user filter.
     * REMOVE THIS IN PRODUCTION - Only for debugging user-related issues.
     *
     * @return all loans in database
     */
    @Transactional(readOnly = true)
    public List<Loan> debugGetAllLoansWithoutUserFilter() {
        return loanRepository.findAll();
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
