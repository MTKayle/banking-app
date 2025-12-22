package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.AccountInfoResponse;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.api.dto.CreateSavingRequest;
import com.example.mobilebanking.api.dto.CreateSavingResponse;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.api.dto.MySavingAccountDTO;
import com.example.mobilebanking.api.dto.QRCodeRequest;
import com.example.mobilebanking.api.dto.SavingAccountDTO;
import com.example.mobilebanking.api.dto.SavingTermDTO;
import com.example.mobilebanking.api.dto.SavingTermsResponse;
import com.example.mobilebanking.api.dto.WithdrawConfirmResponse;
import com.example.mobilebanking.api.dto.WithdrawPreviewResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Account API Service
 */
public interface AccountApiService {
    
    /**
     * Lấy thông tin tài khoản checking theo userId
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("accounts/{userId}/checking")
    Call<CheckingAccountInfoResponse> getCheckingAccountInfo(@Path("userId") Long userId);
    
    /**
     * Lấy danh sách tài khoản tiết kiệm theo userId
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("saving/accounts/user/{userId}")
    Call<List<SavingAccountDTO>> getSavingAccounts(@Path("userId") Long userId);
    
    /**
     * Lấy danh sách tài khoản vay theo userId
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("mortgage/user/{userId}")
    Call<List<MortgageAccountDTO>> getMortgageAccounts(@Path("userId") Long userId);
    
    /**
     * Lấy danh sách khoản vay theo userId (API mới)
     * Header cần có: Authorization: Bearer {token}
     * Endpoint: http://localhost:8089/api/mortgage/user/{userId}
     */
    @GET("mortgage/user/{userId}")
    Call<List<MortgageAccountDTO>> getMortgagesByUserId(@Path("userId") Long userId);
    
    /**
     * Lấy chi tiết khoản vay theo mortgageId
     * Header cần có: Authorization: Bearer {token}
     * Endpoint: http://localhost:8089/api/mortgage/{mortgageId}
     */
    @GET("mortgage/{mortgageId}")
    Call<MortgageAccountDTO> getMortgageDetail(@Path("mortgageId") Long mortgageId);
    
    /**
     * Lấy thông tin chi tiết tài khoản theo accountNumber
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("accounts/info/{accountNumber}")
    Call<AccountInfoResponse> getAccountInfo(@Path("accountNumber") String accountNumber);
    
    /**
     * Lấy danh sách kỳ hạn và lãi suất tiết kiệm
     * Public endpoint - không cần token
     */
    @GET("saving/terms")
    Call<SavingTermsResponse> getSavingTerms();
    
    /**
     * Tạo sổ tiết kiệm mới
     * Header cần có: Authorization: Bearer {token}
     */
    @POST("saving/create")
    Call<CreateSavingResponse> createSaving(@Body CreateSavingRequest request);
    
    /**
     * Lấy danh sách sổ tiết kiệm của user hiện tại
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("saving/my-accounts")
    Call<List<MySavingAccountDTO>> getMySavingAccounts();
    
    /**
     * Lấy chi tiết sổ tiết kiệm theo savingBookNumber
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("saving/{savingBookNumber}")
    Call<MySavingAccountDTO> getSavingDetail(@Path("savingBookNumber") String savingBookNumber);
    
    /**
     * Xem trước thông tin rút tiền tiết kiệm
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("saving/{savingBookNumber}/withdraw-preview")
    Call<WithdrawPreviewResponse> getWithdrawPreview(@Path("savingBookNumber") String savingBookNumber);
    
    /**
     * Xác nhận rút tiền tiết kiệm
     * Header cần có: Authorization: Bearer {token}
     */
    @POST("saving/{savingBookNumber}/withdraw-confirm")
    Call<WithdrawConfirmResponse> confirmWithdraw(@Path("savingBookNumber") String savingBookNumber);
    
    /**
     * Lấy QR code cho tài khoản checking
     * Header cần có: Authorization: Bearer {token}
     * Response: Image (PNG)
     */
    @POST("accounts/checking/qr-code")
    Call<ResponseBody> getCheckingQRCode(@Body QRCodeRequest request);
}


