package com.reminisense.fa.utils;

import com.reminisense.fa.models.Asset;
import com.reminisense.fa.models.LoginInfo;
import com.reminisense.fa.models.LoginResult;
import com.reminisense.fa.models.RestResult;
import com.reminisense.fa.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit interface definitions to communicate with our API.
 */
public interface FeatherAssetsWebService {

    String BASE_URL = "http://feather-assets.herokuapp.com/";
    String X_AUTH_TOKEN = "X-Auth-Token";

    @POST("/api/asset/add")
    Call<RestResult> registerAsset(@Body Asset asset, @Header(X_AUTH_TOKEN) String xAuthToken);

    @GET("/api/verify")
    Call<Asset> verify(@Path("tag") String tag, @Header(X_AUTH_TOKEN) String xAuthToken);

    @POST("/login")
    Call<LoginResult> login(@Body LoginInfo loginInfo);

    /*
    test post below
     */
    @POST("/api/user/add")
    Call<RestResult> registerUser(@Body User user);

    /*@GET("/user")
    Call<User> getUserId (@Path("userId") String userId);
    */
}