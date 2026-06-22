package com.example.springboot.controller;

import com.example.springboot.dto.ProductUpsertRequest;
import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category) {
        AuthContext.require();
        return productService.list(category);
    }

    @GetMapping("/{id}")
    public ProductDetail detail(@PathVariable Integer id) {
        AuthContext.require();
        return productService.detail(id);
    }

    @PostMapping
    public Product create(@Valid @RequestBody ProductUpsertRequest req) {
        AuthContext.requireStaff();
        return productService.create(req);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id, @Valid @RequestBody ProductUpsertRequest req) {
        AuthContext.requireStaff();
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        AuthContext.requireStaff();
        productService.delete(id);
    }
}
