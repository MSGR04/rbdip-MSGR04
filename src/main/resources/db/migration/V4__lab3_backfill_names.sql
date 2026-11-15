UPDATE customers
SET first_name = split_part(btrim(full_name), ' ', 1),
    last_name = btrim(substr(
            btrim(full_name),
            length(split_part(btrim(full_name), ' ', 1)) + 1))
WHERE full_name IS NOT NULL
  AND (first_name IS NULL OR last_name IS NULL);

UPDATE customers SET first_name = '' WHERE first_name IS NULL;
UPDATE customers SET last_name = '' WHERE last_name IS NULL;

ALTER TABLE customers ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE customers ALTER COLUMN last_name SET NOT NULL;
