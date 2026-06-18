package com.example.springboot.service;

import com.example.springboot.dto.ProductUpsertRequest;
import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import com.example.springboot.mapper.ProductMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> list(String category) {
        return productMapper.findAll(category);
    }

    public ProductDetail detail(Integer id) {
        ProductDetail d = productMapper.findDetailById(id);
        if (d == null) {
            throw new com.example.springboot.common.BizException(404, "商品不存在");
        }
        // 台式机整机：附带组装配置
        if ("台式机整机".equals(d.getCategory())) {
            List<Map<String, Object>> comp = productMapper.findCompositionByProductId(id);
            d.setComposition(comp);
        }
        return d;
    }

    public Product create(ProductUpsertRequest req) {
        validateCategory(req.getCategory());
        Product p = new Product();
        p.setBrand(req.getBrand());
        p.setModel(req.getModel());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setCategory(req.getCategory());
        productMapper.insert(p);
        insertDetailByCategory(p.getProductId(), req);
        return p;
    }

    public Product update(Integer id, ProductUpsertRequest req) {
        Product p = productMapper.findBasicById(id);
        if (p == null) {
            throw new com.example.springboot.common.BizException(404, "商品不存在");
        }
        // 不允许修改分类（详情表结构差异，改分类需重建详情）
        if (!p.getCategory().equals(req.getCategory())) {
            throw new com.example.springboot.common.BizException("不允许修改商品分类");
        }
        p.setBrand(req.getBrand());
        p.setModel(req.getModel());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        productMapper.updateBasic(p);
        updateDetailByCategory(id, req, p.getCategory());
        return p;
    }

    public void delete(Integer id) {
        Product p = productMapper.findBasicById(id);
        if (p == null) {
            throw new com.example.springboot.common.BizException(404, "商品不存在");
        }
        try {
            productMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new com.example.springboot.common.BizException("该商品存在订单明细或被台式机配置引用，无法删除");
        }
    }

    private void validateCategory(String category) {
        if (!List.of("笔记本", "台式机整机", "DIY配件").contains(category)) {
            throw new com.example.springboot.common.BizException("分类必须为 笔记本/台式机整机/DIY配件");
        }
    }

    private void insertDetailByCategory(Integer productId, ProductUpsertRequest req) {
        switch (req.getCategory()) {
            case "笔记本" -> productMapper.insertLaptopDetail(productId, req.getScreenSize(),
                    req.getCpuModel(), req.getGpuModel(), req.getWeight());
            case "台式机整机" -> productMapper.insertDesktopDetail(productId, req.getFormFactor(),
                    req.getCpuDesc(), req.getGpuDesc(), req.getRamDesc(), req.getStorageDesc());
            case "DIY配件" -> productMapper.insertSparePartDetail(productId, req.getPartType(),
                    req.getSpecification());
        }
    }

    private void updateDetailByCategory(Integer productId, ProductUpsertRequest req, String category) {
        switch (category) {
            case "笔记本" -> {
                int n = productMapper.updateLaptopDetail(productId, req.getScreenSize(),
                        req.getCpuModel(), req.getGpuModel(), req.getWeight());
                if (n == 0) {
                    productMapper.insertLaptopDetail(productId, req.getScreenSize(),
                            req.getCpuModel(), req.getGpuModel(), req.getWeight());
                }
            }
            case "台式机整机" -> {
                int n = productMapper.updateDesktopDetail(productId, req.getFormFactor(),
                        req.getCpuDesc(), req.getGpuDesc(), req.getRamDesc(), req.getStorageDesc());
                if (n == 0) {
                    productMapper.insertDesktopDetail(productId, req.getFormFactor(),
                            req.getCpuDesc(), req.getGpuDesc(), req.getRamDesc(), req.getStorageDesc());
                }
            }
            case "DIY配件" -> {
                int n = productMapper.updateSparePartDetail(productId, req.getPartType(),
                        req.getSpecification());
                if (n == 0) {
                    productMapper.insertSparePartDetail(productId, req.getPartType(),
                            req.getSpecification());
                }
            }
        }
    }
}
