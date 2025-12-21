package com.example.mobilebanking.api.dto;

/**
 * External Transfer Initiate Response
 * Response for external transfer /api/external-transfer/initiate
 */
public class ExternalTransferInitiateResponse {
    private ExternalTransferData data;
    private boolean success;
    private String message;

    public ExternalTransferInitiateResponse() {}

    public ExternalTransferData getData() {
        return data;
    }

    public void setData(ExternalTransferData data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class ExternalTransferData {
        private String transactionCode;
        private String message;

        public ExternalTransferData() {}

        public String getTransactionCode() {
            return transactionCode;
        }

        public void setTransactionCode(String transactionCode) {
            this.transactionCode = transactionCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
