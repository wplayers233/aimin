package com.oimc.aimin.base.model.drug.index;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(indexName = "drug_index")
public class DrugIndex implements Serializable {

    /**
     * 药品ID，主键，自增
     */
    @Id
    private Integer drugId;

    /**
     * 药品名称，例如“阿莫西林胶囊”
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;

    /**
     * 药品通用名，药品的标准学名
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String genericName;

    /**
     * 分类ID，与 t_drug_categories 表关联
     */
    @Field(type = FieldType.Integer)
    private Integer categoryId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String categoryName;

    /**
     * 品牌名称，例如“辉瑞”
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String brand;

    /**
     * 生产厂家，例如“华东制药”
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String manufacturer;

    /**
     * 药品描述，详细说明药品的用途和特点
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;

    /**
     * 剂型，例如“片剂”、“胶囊”、“注射液”
     */
    @Field(type = FieldType.Text)
    private String dosageForm;

    /**
     * 规格，例如“500mg”、“100单位/ml”
     */
    @Field(type = FieldType.Text)
    private String strength;

    /**
     * 售价，单位为人民币元
     */
    @Field(type = FieldType.Keyword)
    private BigDecimal price;

    /**
     * 折扣价，可为空
     */
    @Field(type = FieldType.Keyword)
    private BigDecimal discountPrice;


    /**
     * 是否需要处方，TRUE 表示需要，FALSE 表示不需要
     */
    @Field(type = FieldType.Boolean)
    private Boolean prescription;


    @Field(type = FieldType.Text)
    private String shelfLife;


    @Field(type = FieldType.Auto)
    private String barCode;


    /**
     * 储存说明，例如“避光保存”或“冷藏保存”
     */
    @Field(type = FieldType.Text)
    private String storageInstructions;

    /**
     * 药品创建时间，默认当前时间
     */
    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "yyyy-MM-dd'T'HH:mm:ssZZZZZ || yyyy-MM-dd HH:mm:ss || epoch_millis"
    )
    private Date createAt;


    /**
     * 药品更新时间，自动更新为当前时间
     */
    @Field(
            type = FieldType.Date,
            format = {},
            pattern = "yyyy-MM-dd'T'HH:mm:ssZZZZZ || yyyy-MM-dd HH:mm:ss || epoch_millis"
    )
    private Date updateAt;

    /**
     * 药品状态，“Available”表示上架，“Unavailable”表示下架
     */
    @Field(type = FieldType.Integer, storeNullValue = true)
    private Integer status;

    /**
     * 是否逻辑删除，TRUE 表示已删除，FALSE 表示未删除
     */
    @Field(type = FieldType.Boolean)
    private Boolean deleted;

    @Field(type = FieldType.Text)
    private String imgUrl;

}
