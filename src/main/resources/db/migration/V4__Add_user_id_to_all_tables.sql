-- Add user_id column to accounts table (nullable first, then set default)
ALTER TABLE accounts 
ADD COLUMN user_id BIGINT NULL AFTER is_include_in_balance;

-- Update existing records to use the first user (or you can delete existing data)
UPDATE accounts SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Now make it NOT NULL and add foreign key
ALTER TABLE accounts 
MODIFY COLUMN user_id BIGINT NOT NULL,
ADD CONSTRAINT fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Add user_id column to categories table (nullable for system categories)
ALTER TABLE categories 
ADD COLUMN user_id BIGINT AFTER is_active,
ADD CONSTRAINT fk_categories_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_categories_user_id ON categories(user_id);

-- Add user_id column to transactions table (nullable first)
ALTER TABLE transactions 
ADD COLUMN user_id BIGINT NULL AFTER recurring_end_date;

-- Update existing records
UPDATE transactions SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Now make it NOT NULL and add foreign key
ALTER TABLE transactions 
MODIFY COLUMN user_id BIGINT NOT NULL,
ADD CONSTRAINT fk_transactions_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);

-- Add user_id column to loans table (nullable first)
ALTER TABLE loans 
ADD COLUMN user_id BIGINT NULL AFTER next_reminder_date;

-- Update existing records
UPDATE loans SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Now make it NOT NULL and add foreign key
ALTER TABLE loans 
MODIFY COLUMN user_id BIGINT NOT NULL,
ADD CONSTRAINT fk_loans_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_loans_user_id ON loans(user_id);

-- Add user_id column to recurring_expenses table (nullable first)
ALTER TABLE recurring_expenses 
ADD COLUMN user_id BIGINT NULL AFTER notes;

-- Update existing records
UPDATE recurring_expenses SET user_id = (SELECT MIN(id) FROM users) WHERE user_id IS NULL;

-- Now make it NOT NULL and add foreign key
ALTER TABLE recurring_expenses 
MODIFY COLUMN user_id BIGINT NOT NULL,
ADD CONSTRAINT fk_recurring_expenses_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);

