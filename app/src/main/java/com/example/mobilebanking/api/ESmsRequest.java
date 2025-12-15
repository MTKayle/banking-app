package com.example.mobilebanking.api;

/**
 * Request model for eSMS API
 */
public class ESmsRequest {
    private String ApiKey;
    private String SecretKey;
    private String Phone;
    private String Content;
    private String Brandname;
    private String SmsType;
    private String IsUnicode;
    private String Sandbox;
    private String RequestId;
    private String SendDate;
    private String campaignid;
    private String CallbackUrl;
    
    public ESmsRequest() {
    }
    
    // Getters and Setters
    public String getApiKey() {
        return ApiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.ApiKey = apiKey;
    }
    
    public String getSecretKey() {
        return SecretKey;
    }
    
    public void setSecretKey(String secretKey) {
        this.SecretKey = secretKey;
    }
    
    public String getPhone() {
        return Phone;
    }
    
    public void setPhone(String phone) {
        this.Phone = phone;
    }
    
    public String getContent() {
        return Content;
    }
    
    public void setContent(String content) {
        this.Content = content;
    }
    
    public String getBrandname() {
        return Brandname;
    }
    
    public void setBrandname(String brandname) {
        this.Brandname = brandname;
    }
    
    public String getSmsType() {
        return SmsType;
    }
    
    public void setSmsType(String smsType) {
        this.SmsType = smsType;
    }
    
    public String getIsUnicode() {
        return IsUnicode;
    }
    
    public void setIsUnicode(String isUnicode) {
        this.IsUnicode = isUnicode;
    }
    
    public String getSandbox() {
        return Sandbox;
    }
    
    public void setSandbox(String sandbox) {
        this.Sandbox = sandbox;
    }
    
    public String getRequestId() {
        return RequestId;
    }
    
    public void setRequestId(String requestId) {
        this.RequestId = requestId;
    }
    
    public String getSendDate() {
        return SendDate;
    }
    
    public void setSendDate(String sendDate) {
        this.SendDate = sendDate;
    }
    
    public String getCampaignid() {
        return campaignid;
    }
    
    public void setCampaignid(String campaignid) {
        this.campaignid = campaignid;
    }
    
    public String getCallbackUrl() {
        return CallbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.CallbackUrl = callbackUrl;
    }
}

