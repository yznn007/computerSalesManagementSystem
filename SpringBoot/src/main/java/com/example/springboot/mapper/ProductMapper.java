package com.example.springboot.mapper;

import com.example.springboot.entity.Product;
import com.example.springboot.entity.ProductDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface ProductMapper {

    @Select("<script>" +
            "SELECT * FROM Product " +
            "<where> " +
            "  <if test='category != null and category != \"\"'>category = #{category}</if> " +
            "</where> " +
            "ORDER BY product_id" +
            "</script>")
    List<Product> findAll(@Param("category") String category);

    @Select("SELECT p.* FROM Product p WHERE p.product_id = #{id}")
    Product findBasicById(@Param("id") Integer id);

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

    @Select("SELECT dc.quantity, " +
            "       pp.product_id, pp.brand, pp.model, pp.category, " +
            "       spd.part_type, spd.specification " +
            "FROM Desktop_Composition dc " +
            "JOIN Spare_Part_Detail spd ON dc.part_id = spd.part_id " +
            "JOIN Product pp ON spd.product_id = pp.product_id " +
            "WHERE dc.product_id = #{productId}")
    List<Map<String, Object>> findCompositionByProductId(@Param("productId") Integer productId);

    @Insert("INSERT INTO Product (brand, model, price, stock, category) " +
            "VALUES (#{brand}, #{model}, #{price}, #{stock}, #{category})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insert(Product p);

    @Update("UPDATE Product SET brand=#{brand}, model=#{model}, price=#{price}, stock=#{stock} " +
            "WHERE product_id = #{productId}")
    int updateBasic(Product p);

    @Delete("DELETE FROM Product WHERE product_id = #{id}")
    int deleteById(@Param("id") Integer id);

    // 笔记本详情
    @Insert("INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight) " +
            "VALUES (#{productId}, #{screenSize}, #{cpuModel}, #{gpuModel}, #{weight})")
    int insertLaptopDetail(@Param("productId") Integer productId, @Param("screenSize") String screenSize,
                           @Param("cpuModel") String cpuModel, @Param("gpuModel") String gpuModel,
                           @Param("weight") String weight);

    @Update("UPDATE Laptop_Detail SET screen_size=#{screenSize}, cpu_model=#{cpuModel}, gpu_model=#{gpuModel}, weight=#{weight} " +
            "WHERE product_id = #{productId}")
    int updateLaptopDetail(@Param("productId") Integer productId, @Param("screenSize") String screenSize,
                           @Param("cpuModel") String cpuModel, @Param("gpuModel") String gpuModel,
                           @Param("weight") String weight);

    // 台式机整机详情
    @Insert("INSERT INTO Desktop_Detail (product_id, form_factor, cpu_desc, gpu_desc, ram_desc, storage_desc) " +
            "VALUES (#{productId}, #{formFactor}, #{cpuDesc}, #{gpuDesc}, #{ramDesc}, #{storageDesc})")
    int insertDesktopDetail(@Param("productId") Integer productId, @Param("formFactor") String formFactor,
                            @Param("cpuDesc") String cpuDesc, @Param("gpuDesc") String gpuDesc,
                            @Param("ramDesc") String ramDesc, @Param("storageDesc") String storageDesc);

    @Update("UPDATE Desktop_Detail SET form_factor=#{formFactor}, cpu_desc=#{cpuDesc}, gpu_desc=#{gpuDesc}, ram_desc=#{ramDesc}, storage_desc=#{storageDesc} " +
            "WHERE product_id = #{productId}")
    int updateDesktopDetail(@Param("productId") Integer productId, @Param("formFactor") String formFactor,
                            @Param("cpuDesc") String cpuDesc, @Param("gpuDesc") String gpuDesc,
                            @Param("ramDesc") String ramDesc, @Param("storageDesc") String storageDesc);

    // DIY配件详情
    @Insert("INSERT INTO Spare_Part_Detail (product_id, part_type, specification) " +
            "VALUES (#{productId}, #{partType}, #{specification})")
    int insertSparePartDetail(@Param("productId") Integer productId, @Param("partType") String partType,
                              @Param("specification") String specification);

    @Update("UPDATE Spare_Part_Detail SET part_type=#{partType}, specification=#{specification} " +
            "WHERE product_id = #{productId}")
    int updateSparePartDetail(@Param("productId") Integer productId, @Param("partType") String partType,
                              @Param("specification") String specification);
}
