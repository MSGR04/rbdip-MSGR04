DROP TRIGGER lab3_customers_name_sync ON customers;
DROP FUNCTION lab3_sync_customer_name();

ALTER TABLE customers DROP COLUMN full_name;
