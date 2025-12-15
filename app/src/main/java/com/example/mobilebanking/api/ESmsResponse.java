package com.example.mobilebanking.api;

/**
 * Response model for eSMS API
 */
public class ESmsResponse {
    private String CodeResult;
    private String SMSID;
    private String ErrorMessage;
    private int CountRegenerate;
    
    public ESmsResponse() {
    }
    
    // Getters and Setters
    public String getCodeResult() {
        return CodeResult;
    }
    
    public void setCodeResult(String codeResult) {
        this.CodeResult = codeResult;
    }
    
    public String getSMSID() {
        return SMSID;
    }
    
    public void setSMSID(String SMSID) {
        this.SMSID = SMSID;
    }
    
    public String getErrorMessage() {
        return ErrorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.ErrorMessage = errorMessage;
    }
    
    public int getCountRegenerate() {
        return CountRegenerate;
    }
    
    public void setCountRegenerate(int countRegenerate) {
        this.CountRegenerate = countRegenerate;
    }
    
    public boolean isSuccess() {
        return "100".equals(CodeResult);
    }
}

