ALTER TABLE customers ADD COLUMN first_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN last_name VARCHAR(255);

ALTER TABLE customers ALTER COLUMN full_name DROP NOT NULL;

CREATE FUNCTION lab3_sync_customer_name() RETURNS trigger AS $$
BEGIN
    IF NEW.full_name IS NOT NULL
            AND (TG_OP = 'INSERT' OR NEW.full_name IS DISTINCT FROM OLD.full_name) THEN
        NEW.first_name := split_part(btrim(NEW.full_name), ' ', 1);
        NEW.last_name := btrim(substr(
                btrim(NEW.full_name),
                length(split_part(btrim(NEW.full_name), ' ', 1)) + 1));
    ELSIF NEW.first_name IS NOT NULL
            AND NEW.last_name IS NOT NULL
            AND (TG_OP = 'INSERT'
                OR NEW.first_name IS DISTINCT FROM OLD.first_name
                OR NEW.last_name IS DISTINCT FROM OLD.last_name) THEN
        NEW.full_name := btrim(btrim(NEW.first_name) || ' ' || btrim(NEW.last_name));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER lab3_customers_name_sync
BEFORE INSERT OR UPDATE ON customers
FOR EACH ROW EXECUTE FUNCTION lab3_sync_customer_name();
