CREATE TABLE IF NOT EXISTS stock_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    note VARCHAR(500),
    entry_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_stock_entries_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX IF NOT EXISTS idx_stock_entries_tenant_id ON stock_entries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_stock_entries_product_id ON stock_entries(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_entries_tenant_entry_date ON stock_entries(tenant_id, entry_date);
