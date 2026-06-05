CREATE TABLE IF NOT EXISTS production_batches (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity_kg NUMERIC(12,3) NOT NULL,
    batch_date DATE NOT NULL,
    note VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_production_batches_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_production_batches_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX IF NOT EXISTS idx_production_batches_tenant_id ON production_batches(tenant_id);
CREATE INDEX IF NOT EXISTS idx_production_batches_product_id ON production_batches(product_id);
CREATE INDEX IF NOT EXISTS idx_production_batches_tenant_batch_date ON production_batches(tenant_id, batch_date);
