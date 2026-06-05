CREATE TABLE IF NOT EXISTS finance_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(120) NOT NULL,
    note TEXT,
    amount NUMERIC(19,2) NOT NULL,
    entry_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_finance_entries_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX IF NOT EXISTS idx_finance_entries_tenant_id ON finance_entries(tenant_id);
CREATE INDEX IF NOT EXISTS idx_finance_entries_entry_date ON finance_entries(entry_date);
CREATE INDEX IF NOT EXISTS idx_finance_entries_type ON finance_entries(type);
CREATE INDEX IF NOT EXISTS idx_finance_entries_category ON finance_entries(category);
