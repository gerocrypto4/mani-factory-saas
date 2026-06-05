package com.manifactory.backend.exception;

import com.manifactory.backend.stock.dto.StockShortageDTO;
import java.util.List;

public class InsufficientStockException extends RuntimeException {

    private final List<StockShortageDTO> shortages;

    public InsufficientStockException(List<StockShortageDTO> shortages) {
        super("Stock insuficiente para confirmar el pedido");
        this.shortages = shortages;
    }

    public List<StockShortageDTO> getShortages() {
        return shortages;
    }
}
