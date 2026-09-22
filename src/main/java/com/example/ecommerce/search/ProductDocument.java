package com.example.ecommerce.search;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String category;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Long)
    private Long vendorId;

    @Field(type = FieldType.Text)
    private String vendorName;

    @Field(type = FieldType.Double)
    private Double ratingAverage;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    public ProductDocument() {
    }

    public ProductDocument(
            String id,
            String name,
            String category,
            BigDecimal price,
            Integer stock,
            Long vendorId,
            String vendorName,
            Double ratingAverage,
            Integer reviewCount,
            String imageUrl) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price != null ? price.doubleValue() : null;
        this.stock = stock;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.ratingAverage = ratingAverage;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public Double getRatingAverage() {
        return ratingAverage;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}