-- =====================================================
-- Performance Optimization Indexes
-- Created to support N+1 query optimizations
-- =====================================================
-- NOTE: V1 and V2 already created many basic indexes.
-- This migration only adds NEW composite indexes for user-based queries
-- that were added when user_id columns were introduced in V4.
-- =====================================================

-- Transaction indexes (user-based, NEW in V4)
-- Index for finding transactions by user and date (most common query)
CREATE INDEX idx_transactions_user_date 
ON transactions(user_id, transaction_date DESC);

-- Index for finding transactions by type (used in income/expense reports)
CREATE INDEX idx_transactions_user_type 
ON transactions(user_id, type);

-- =====================================================
-- Recurring Expense indexes (user-based, NEW in V4)
-- =====================================================

-- Index for finding active recurring expenses by user
CREATE INDEX idx_recurring_expenses_user_status 
ON recurring_expenses(user_id, status);

-- Index for finding due/overdue expenses by status (enhanced from V2)
CREATE INDEX idx_recurring_expenses_next_due_status 
ON recurring_expenses(next_due_date, status);

-- =====================================================
-- Loan indexes (user-based, NEW in V4)
-- =====================================================

-- Index for finding loans by user and type (lent vs borrowed)
CREATE INDEX idx_loans_user_type 
ON loans(user_id, loan_type);

-- Index for finding loans by user and status
CREATE INDEX idx_loans_user_status 
ON loans(user_id, status);

-- Index for finding overdue loans (enhanced with status from V1)
CREATE INDEX idx_loans_due_date_status 
ON loans(due_date, status);

-- Index for finding urgent loans (enhanced from V1)
CREATE INDEX idx_loans_urgent_status 
ON loans(is_urgent, status);

-- =====================================================
-- Account indexes (user-based, NEW in V4)
-- =====================================================

-- Index for finding active accounts by user
CREATE INDEX idx_accounts_user_status 
ON accounts(user_id, status);

-- Index for finding accounts by user and type (for type-specific queries)
CREATE INDEX idx_accounts_user_type 
ON accounts(user_id, type);

-- Index for balance calculations (accounts included in total balance)
CREATE INDEX idx_accounts_balance_calc 
ON accounts(user_id, is_include_in_balance, status);

-- =====================================================
-- Category indexes (user-based, NEW in V4)
-- =====================================================

-- Index for finding categories by user (includes system categories where user_id is NULL)
CREATE INDEX idx_categories_user 
ON categories(user_id);

-- Index for finding categories by type and active status (enhanced from V1)
CREATE INDEX idx_categories_type_active 
ON categories(type, is_active);

-- =====================================================
-- Composite indexes for complex dashboard queries
-- =====================================================

-- For dashboard queries: recent transactions by user
CREATE INDEX idx_transactions_dashboard 
ON transactions(user_id, transaction_date DESC, type);

-- For expense tracking: expenses by user, category, and date
CREATE INDEX idx_transactions_expense_tracking 
ON transactions(user_id, category_id, transaction_date DESC);

-- For income tracking: income by user and date
CREATE INDEX idx_transactions_income_tracking 
ON transactions(user_id, transaction_date DESC);

-- =====================================================
-- Performance Notes:
-- =====================================================
-- 1. These indexes significantly improve query performance for user-scoped queries
-- 2. Trade-off: Slightly slower INSERT/UPDATE operations
-- 3. Disk space: Indexes require additional storage
-- 4. Maintenance: MySQL automatically updates indexes
-- 5. V1 and V2 already created basic indexes - this adds composite indexes for user filtering
--
-- To check index usage:
-- SELECT * FROM performance_schema.table_io_waits_summary_by_index_usage 
-- WHERE object_schema = 'financial';
--
-- To check index size:
-- SELECT 
--     TABLE_NAME,
--     INDEX_NAME,
--     ROUND(STAT_VALUE * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
-- FROM mysql.innodb_index_stats
-- WHERE database_name = 'financial'
-- AND stat_name = 'size';
-- =====================================================
