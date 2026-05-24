DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_order_items_product'
          AND table_name = 'order_items'
    ) THEN
        ALTER TABLE order_items
            ADD CONSTRAINT fk_order_items_product
            FOREIGN KEY (product_id) REFERENCES products(id);
    END IF;
END $$;
