package com.delivery.fragment.task.entity;

import org.json.JSONObject;

public class ProductsEntity {

    private String productName;
    private String brandName;
    private String imageUrl;
    private String amount;
    private String quantity;
    private int productId;
    private int productListingId;
    private String skuCode;
    private JSONObject jsonObject;

    public void setJsonObject(JSONObject jsonObject)
    {
        this.jsonObject=jsonObject;
    }
    public  JSONObject getJsonObject()
    {
        return jsonObject;
    }
    public String getSkuCode()
    {
        return skuCode;
    }
    public void setSkuCode(String skuCode)
    {
        this.skuCode=skuCode;
    }
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
    public int getProductListingId() {
        return productListingId;
    }

    public void setProductListingId(int productListingId) {
        this.productListingId = productListingId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductQuantity() {
        return quantity;
    }

    public void setProductQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setBrandName(String brandName) {
        this.brandName =brandName;
    }
    public String getBrandName() {
        return brandName;
    }

    public String getImageUrl(){return imageUrl;}

    public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

}
