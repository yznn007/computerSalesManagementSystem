package com.example.springboot.service;

import com.example.springboot.common.BizException;
import com.example.springboot.dto.ProductUpsertRequest;
import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import com.example.springboot.mapper.ProductMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 商品管理业务层（仅供销售员）。
 * 商品按 category 分三类（笔记本/台式机整机/DIY配件），各对应一张详情子表；
 * 增改时先维护主表再按分类分发到对应详情表。分类一旦创建不可修改。
 */
@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /** 商品列表（可按分类过滤） */
    public List<Product> list(String category) {
        return productMapper.findAll(category);
    }

    /** 商品详情：台式机整机额外附带其配件组成清单（BOM） */
    public ProductDetail detail(Integer id) {
        ProductDetail d = productMapper.findDetailById(id);
        if (d == null) {
            throw new BizException(404, "商品不存在");
        }
        // 台式机整机：附带组装配置
        if ("台式机整机".equals(d.getCategory())) {
            List<Map<String, Object>> comp = productMapper.findCompositionByProductId(id);
            d.setComposition(comp);
        }
        return d;
    }

    /** 新建商品：校验分类合法后写主表，再按分类写对应详情子表 */
    public Product create(ProductUpsertRequest req) {
        validateCategory(req.getCategory());
        Product p = new Product();
        p.setBrand(req.getBrand());
        p.setModel(req.getModel());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        p.setCategory(req.getCategory());
        productMapper.insert(p); // 自增主键回填到 p.productId，供详情子表外键引用
        insertDetailByCategory(p.getProductId(), req);
        return p;
    }

    /** 更新商品：分类不可变，更新主表基础字段后同步详情子表 */
    public Product update(Integer id, ProductUpsertRequest req) {
        Product p = productMapper.findBasicById(id);
        if (p == null) {
            throw new BizException(404, "商品不存在");
        }
        // 不允许修改分类（详情表结构差异，改分类需重建详情）
        if (!p.getCategory().equals(req.getCategory())) {
            throw new BizException("不允许修改商品分类");
        }
        p.setBrand(req.getBrand());
        p.setModel(req.getModel());
        p.setPrice(req.getPrice());
        p.setStock(req.getStock());
        productMapper.updateBasic(p);
        updateDetailByCategory(id, req, p.getCategory());
        return p;
    }

    /** 删除商品：被订单明细或台式机配置引用（外键约束）时拦截并提示 */
    public void delete(Integer id) {
        Product p = productMapper.findBasicById(id);
        if (p == null) {
            throw new BizException(404, "商品不存在");
        }
        try {
            productMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("该商品存在订单明细或被台式机配置引用，无法删除");
        }
    }

    /** 校验商品分类必须为三个固定枚举之一 */
    private void validateCategory(String category) {
        if (!List.of("笔记本", "台式机整机", "DIY配件").contains(category)) {
            throw new BizException("分类必须为 笔记本/台式机整机/DIY配件");
        }
    }

    /** 新建时按分类插入对应详情子表（笔记本/台式整机/配件三选一） */
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

    /** 更新时按分类同步详情子表；若详情行不存在（update 影响 0 行）则补插，等价于 upsert */
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
