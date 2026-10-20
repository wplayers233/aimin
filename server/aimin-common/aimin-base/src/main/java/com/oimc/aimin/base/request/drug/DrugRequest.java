package com.oimc.aimin.base.request.drug;


import com.oimc.aimin.base.enums.DrugStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DrugRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer drugId;

    // 药品名称，不能为空或空白
    @NotBlank(message = "药品名称不能为空")
    private String name;

    // 药品描述，不能为空或空白
    @NotBlank(message = "药品描述不能为空")
    private String description;

    // 药品通用名称，不能为空或空白
    @NotBlank(message = "药品通用名称不能为空")
    private String genericName;

    // 药品分类ID，不能为null
    @NotNull(message = "药品分类ID不能为空")
    private Integer categoryId;

    // 药品品牌，不能为空或空白
    @NotBlank(message = "药品品牌不能为空")
    private String brand;

    // 药品制造商，不能为空或空白
    @NotBlank(message = "药品制造商不能为空")
    private String manufacturer;

    // 药品剂型（如片剂、胶囊等），不能为空或空白
    @NotBlank(message = "药品剂型不能为空")
    private String dosageForm;

    // 药品规格/强度（如500mg），不能为空或空白
    @NotBlank(message = "药品规格/强度不能为空")
    private String strength;

    // 药品价格，不能为null
    @NotNull(message = "药品价格不能为空")
    private BigDecimal price;

    // 药品库存数量（可选）
    private Integer stockQuantity;

    // 是否需要处方，不能为null
    @NotNull(message = "处方要求字段不能为空")
    private Boolean prescription;

    // 药品保质期，不能为null
    @NotNull(message = "药品保质期不能为空")
    private Integer shelfLife;

    // 保质期单位（如天、月、年），不能为null
    @NotNull(message = "保质期单位不能为空")
    private Integer shelfLifeUnit;

    // 药品存储说明（可选）
    private String storageInstructions;

    // 药品状态（如可用、停用等，可选）
    private DrugStatus status;

    private List<Img> imgList;

    @Data
    public static class Img {
        private String path;
        private Boolean isMain;
        private byte sort;
    }
}
