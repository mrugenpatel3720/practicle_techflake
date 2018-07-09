package com.mrugen_practicle.network;


import com.mrugen_practicle.models.Example;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface NetworkInterface {

    @GET("search")
    Observable<Example> getMoviesBasedOnQuery(@Query("api_key") String api_key, @Query("q") String q);
}
