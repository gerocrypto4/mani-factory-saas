package com.manifactory.backend.products.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.manifactory.backend.products.dto.CreateProductDTO;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.dto.UpdateProductDTO;
import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.mapper.ProductMapper;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.products.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class ProductServiceTest {

    private ProductRepository repository;
    private ProductMapper mapper;
    private ProductServiceImpl service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(ProductRepository.class);
        mapper = new ProductMapper();
        service = new ProductServiceImpl(repository, mapper);
    }

    @Test
    void createAndGetProduct() {
        CreateProductDTO create = new CreateProductDTO();
        create.setTenantId(1L);
        create.setName("Test product");
        create.setDescription("desc");
        create.setPrice(BigDecimal.valueOf(9.99));

        Product saved = Product.builder()
                .id(10L)
                .tenantId(1L)
                .name(create.getName())
                .description(create.getDescription())
                .price(create.getPrice())
                .build();

        Mockito.when(repository.save(ArgumentMatchers.any(Product.class))).thenReturn(saved);
        Mockito.when(repository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(saved));

        ProductResponseDTO created = service.create(create);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(10L);

        ProductResponseDTO fetched = service.getById(1L, 10L);
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("Test product");
    }

    @Test
    void updateProduct() {
        Product existing = Product.builder().id(20L).tenantId(2L).name("Old").price(BigDecimal.ONE).build();
        Mockito.when(repository.findByIdAndTenantId(20L, 2L)).thenReturn(Optional.of(existing));
        Mockito.when(repository.save(ArgumentMatchers.any(Product.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProductDTO upd = new UpdateProductDTO();
        upd.setName("New name");

        ProductResponseDTO updated = service.update(2L, 20L, upd);
        assertThat(updated.getName()).isEqualTo("New name");
    }
}
