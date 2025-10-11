-- Add user_id column to accounts table
ALTER TABLE accounts ADD COLUMN user_id BIGINT NULL;

-- Update existing records to use the first user (or you can delete existing data)
UPDATE accounts SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Make it NOT NULL
ALTER TABLE accounts MODIFY COLUMN user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE accounts ADD CONSTRAINT fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Add user_id column to categories table (nullable for system categories)
ALTER TABLE categories ADD COLUMN user_id BIGINT;

-- Add foreign key constraint
ALTER TABLE categories ADD CONSTRAINT fk_categories_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_categories_user_id ON categories(user_id);

-- Add user_id column to transactions table
ALTER TABLE transactions ADD COLUMN user_id BIGINT NULL;

-- Update existing records
UPDATE transactions SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Make it NOT NULL
ALTER TABLE transactions MODIFY COLUMN user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);

-- Add user_id column to loans table
ALTER TABLE loans ADD COLUMN user_id BIGINT NULL;

-- Update existing records
UPDATE loans SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Make it NOT NULL
ALTER TABLE loans MODIFY COLUMN user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE loans ADD CONSTRAINT fk_loans_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_loans_user_id ON loans(user_id);

-- Add user_id column to recurring_expenses table
ALTER TABLE recurring_expenses ADD COLUMN user_id BIGINT NULL;

-- Update existing records
UPDATE recurring_expenses SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Make it NOT NULL
ALTER TABLE recurring_expenses MODIFY COLUMN user_id BIGINT NOT NULL;

-- Add foreign key constraint
ALTER TABLE recurring_expenses ADD CONSTRAINT fk_recurring_expenses_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);
