package com.example.taskmanager.interfaces;

import com.example.taskmanager.R;
import com.example.taskmanager.quote.Quote;
import com.example.taskmanager.quote.QuoteJsonDeserializer;
import com.example.taskmanager.utility.App;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface QuoteAPI {
    public static List<Quote> quotes = null;
    public static GsonConverterFactory getGsonConverterFactory(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Quote.class, new QuoteJsonDeserializer());
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }

    public static Retrofit getRetrofitInstance(){
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        return new Retrofit.Builder()
                .baseUrl(App.getContext().getResources().getString(R.string.zenquotes_url))
                .addConverterFactory(QuoteAPI.getGsonConverterFactory())
                .client(client)
                .build();
    }

    @GET("/api/quotes")
    public Call<List<Quote>> getQuotes();
}