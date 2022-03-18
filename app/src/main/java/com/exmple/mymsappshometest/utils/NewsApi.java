package com.exmple.mymsappshometest.utils;

import com.exmple.mymsappshometest.responses.NewsSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {

    @GET("v2/everything")
    Call<NewsSearchResponse> searchNews(
            @Query("apiKey") String key,
            @Query("q") String query
    );

}
