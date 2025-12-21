package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for bill types API
 */
public class BillTypesResponse {
    private Boolean success;
    private List<BillType> data;
    private String message;

    public BillTypesResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<BillType> getData() {
        return data;
    }

    public void setData(List<BillType> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Bill type item
     */
    public static class BillType {
        private String displayName;
        private String value;

        public BillType() {
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
