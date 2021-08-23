package com.delivery.fragment.task.entity;

public class TaskEntity {

    private String requestId;
    private String location;
    private String latitude;
    private String longitude;

    private String customerLatitude;
    private String customerLongitude;
    private String imageUrl;
    private String requestAcceptedTime;
    private String dueTime;
    private String amount;
    private String mode;
    private String supplier;
    private String customer;
    private String customerContact;
    private String status;
    private String description;
    private String cartProductRequests;
    private String refundedProducts;
    private String refundedProductsAmount;
    private String unpaidAmount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(String unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }
    public void setCartProductRequests(String cartProductRequests) {
        this.cartProductRequests = cartProductRequests;
    }
    public String getCartProductRequests() {
        return cartProductRequests;
    }

    public void setRefundedProducts(String refundedProducts) {
        this.refundedProducts = refundedProducts;
    }
    public String getRefundedProducts() {
        return refundedProducts;
    }

    public String getRefundedProductsAmount() {
        return refundedProductsAmount;
    }

    public void setRefundedProductsAmount(String refundedProductsAmount) {
        this.refundedProductsAmount = refundedProductsAmount;
    }
    public String getDescription(){return description;}

    public void setDescription(String description){this.description=description;}

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRequestAcceptedTime() {
        return requestAcceptedTime;
    }

    public void setRequestAcceptedTime(String requestAcceptedTime) {
        this.requestAcceptedTime = requestAcceptedTime;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(String customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public String getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(String customerLongitude) {
        this.customerLongitude = customerLongitude;
    }
}
