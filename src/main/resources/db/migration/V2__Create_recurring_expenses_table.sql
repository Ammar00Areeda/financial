-- Create recurring_expenses table
CREATE TABLE IF NOT EXISTS recurring_expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(15,2) NOT NULL,
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE,
    next_due_date DATE,
    last_paid_date DATE,
    status ENUM('ACTIVE', 'PAUSED', 'CANCELLED', 'COMPLETED') DEFAULT 'ACTIVE',
    is_auto_pay BOOLEAN DEFAULT FALSE,
    reminder_days_before INT DEFAULT 3,
    provider VARCHAR(200),
    reference_number VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_recurring_expenses_account_id ON recurring_expenses(account_id);
CREATE INDEX idx_recurring_expenses_category_id ON recurring_expenses(category_id);
CREATE INDEX idx_recurring_expenses_frequency ON recurring_expenses(frequency);
CREATE INDEX idx_recurring_expenses_status ON recurring_expenses(status);
CREATE INDEX idx_recurring_expenses_next_due_date ON recurring_expenses(next_due_date);
CREATE INDEX idx_recurring_expenses_start_date ON recurring_expenses(start_date);
CREATE INDEX idx_recurring_expenses_end_date ON recurring_expenses(end_date);
CREATE INDEX idx_recurring_expenses_is_auto_pay ON recurring_expenses(is_auto_pay);
CREATE INDEX idx_recurring_expenses_provider ON recurring_expenses(provider);
CREATE INDEX idx_recurring_expenses_reference_number ON recurring_expenses(reference_number);
