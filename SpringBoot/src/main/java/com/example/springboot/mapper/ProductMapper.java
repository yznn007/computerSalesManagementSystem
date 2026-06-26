package com.example.springboot.mapper;

import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 商品数据访问层。
 * 采用"主表 Product + 三张子类型详情表"结构：笔记本(Laptop_Detail)、台式整机(Desktop_Detail)、
 * DIY 配件(Spare_Part_Detail)。读取详情时按 category 左连接对应子表，写入时分别维护主表与子表。
 */
public interface ProductMapper {

    /** 商品列表（可按 category 过滤），左连配件子表带出 part_type 便于列表展示 */
    @Select("<script>" +
            "SELECT p.*, spd.part_type FROM Product p " +
            "LEFT JOIN Spare_Part_Detail spd ON p.product_id = spd.product_id " +
            "<where> " +
            "  <if test='category != null and category != \"\"'>p.category = #{category}</if> " +
            "</where> " +
            "ORDER BY p.product_id" +
            "</script>")
    List<Product> findAll(@Param("category") String category);

    /** 仅查主表基础信息（不含详情），用于库存/价格等轻量校验 */
    @Select("SELECT p.* FROM Product p WHERE p.product_id = #{id}")
    Product findBasicById(@Param("id") Integer id);

    /** 查商品完整详情：一次性左连三张子表，按 category 决定哪组字段有值 */
    @Select("SELECT p.*, " +
            "  ld.screen_size, ld.cpu_model, ld.gpu_model, ld.weight, " +
            "  dd.form_factor, dd.cpu_desc, dd.gpu_desc, dd.ram_desc, dd.storage_desc, " +
            "  spd.part_type, spd.specification " +
            "FROM Product p " +
            "LEFT JOIN Laptop_Detail ld      ON p.product_id = ld.product_id " +
            "LEFT JOIN Desktop_Detail dd     ON p.product_id = dd.product_id " +
            "LEFT JOIN Spare_Part_Detail spd ON p.product_id = spd.product_id " +
            "WHERE p.product_id = #{id}")
    ProductDetail findDetailById(@Param("id") Integer id);

    /** 查台式机整机的配件清单（BOM）：经组成表 Desktop_Composition 关联到各配件商品 */
    @Select("SELECT dc.quantity, " +
            "       pp.product_id, pp.brand, pp.model, pp.category, " +
            "       spd.part_type, spd.specification " +
            "FROM Desktop_Composition dc " +
            "JOIN Spare_Part_Detail spd ON dc.part_id = spd.part_id " +
            "JOIN Product pp ON spd.product_id = pp.product_id " +
            "WHERE dc.product_id = #{productId}")
    List<Map<String, Object>> findCompositionByProductId(@Param("productId") Integer productId);

    /** 新增商品主表记录，自增主键回填到 productId（再由 Service 写对应子表详情） */
    @Insert("INSERT INTO Product (brand, model, price, stock, category) " +
            "VALUES (#{brand}, #{model}, #{price}, #{stock}, #{category})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insert(Product p);

    /** 更新商品主表基础字段（category 不可改，故不在更新列内） */
    @Update("UPDATE Product SET brand=#{brand}, model=#{model}, price=#{price}, stock=#{stock} " +
            "WHERE product_id = #{productId}")
    int updateBasic(Product p);

    /** 删除商品（关联子表通常由外键级联处理） */
    @Delete("DELETE FROM Product WHERE product_id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 笔记本详情
    /** 插入笔记本详情（屏幕/CPU/GPU/重量） */
    @Insert("INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight) " +
            "VALUES (#{productId}, #{screenSize}, #{cpuModel}, #{gpuModel}, #{weight})")
    int insertLaptopDetail(@Param("productId") Integer productId, @Param("screenSize") String screenSize,
                           @Param("cpuModel") String cpuModel, @Param("gpuModel") String gpuModel,
                           @Param("weight") String weight);

    /** 更新笔记本详情 */
    @Update("UPDATE Laptop_Detail SET screen_size=#{screenSize}, cpu_model=#{cpuModel}, gpu_model=#{gpuModel}, weight=#{weight} " +
            "WHERE product_id = #{productId}")
    int updateLaptopDetail(@Param("productId") Integer productId, @Param("screenSize") String screenSize,
                           @Param("cpuModel") String cpuModel, @Param("gpuModel") String gpuModel,
                           @Param("weight") String weight);

    // 台式机整机详情
    /** 插入台式整机详情（机箱形态/CPU/GPU/内存/存储描述） */
    @Insert("INSERT INTO Desktop_Detail (product_id, form_factor, cpu_desc, gpu_desc, ram_desc, storage_desc) " +
            "VALUES (#{productId}, #{formFactor}, #{cpuDesc}, #{gpuDesc}, #{ramDesc}, #{storageDesc})")
    int insertDesktopDetail(@Param("productId") Integer productId, @Param("formFactor") String formFactor,
                            @Param("cpuDesc") String cpuDesc, @Param("gpuDesc") String gpuDesc,
                            @Param("ramDesc") String ramDesc, @Param("storageDesc") String storageDesc);

    /** 更新台式整机详情 */
    @Update("UPDATE Desktop_Detail SET form_factor=#{formFactor}, cpu_desc=#{cpuDesc}, gpu_desc=#{gpuDesc}, ram_desc=#{ramDesc}, storage_desc=#{storageDesc} " +
            "WHERE product_id = #{productId}")
    int updateDesktopDetail(@Param("productId") Integer productId, @Param("formFactor") String formFactor,
                            @Param("cpuDesc") String cpuDesc, @Param("gpuDesc") String gpuDesc,
                            @Param("ramDesc") String ramDesc, @Param("storageDesc") String storageDesc);

    // DIY配件详情
    /** 插入配件详情（配件类型/规格说明） */
    @Insert("INSERT INTO Spare_Part_Detail (product_id, part_type, specification) " +
            "VALUES (#{productId}, #{partType}, #{specification})")
    int insertSparePartDetail(@Param("productId") Integer productId, @Param("partType") String partType,
                              @Param("specification") String specification);

    /** 更新配件详情 */
    @Update("UPDATE Spare_Part_Detail SET part_type=#{partType}, specification=#{specification} " +
            "WHERE product_id = #{productId}")
    int updateSparePartDetail(@Param("productId") Integer productId, @Param("partType") String partType,
                              @Param("specification") String specification);
}
