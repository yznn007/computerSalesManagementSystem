package com.example.springboot.controller;

import com.example.springboot.dto.ProductUpsertRequest;
import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口（/api/products）。
 * 浏览（list/detail）登录即可；增改删（create/update/delete）仅限销售员。
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 商品列表（可按分类过滤），登录即可访问 */
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category) {
        AuthContext.require();
        return productService.list(category);
    }

    /** 商品详情，登录即可访问 */
    @GetMapping("/{id}")
    public ProductDetail detail(@PathVariable Integer id) {
        AuthContext.require();
        return productService.detail(id);
    }

    /** 新增商品（仅销售员） */
    @PostMapping
    public Product create(@Valid @RequestBody ProductUpsertRequest req) {
        AuthContext.requireStaff();
        return productService.create(req);
    }

    /** 更新商品（仅销售员） */
    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id, @Valid @RequestBody ProductUpsertRequest req) {
        AuthContext.requireStaff();
        return productService.update(id, req);
    }

    /** 删除商品（仅销售员） */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        AuthContext.requireStaff();
        productService.delete(id);
    }
}
