package com.example.taskmanager.interfaces;

import com.example.taskmanager.R;
import com.example.taskmanager.quote.Quote;
import com.example.taskmanager.quote.QuoteJsonDeserializer;
import com.example.taskmanager.utility.App;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * An interface which will be used for making calls to the
 * Zenquotes API using Retrofit.
 */
public interface QuoteAPI {
    List<Quote> quotes = new ArrayList<>();

    /**
     * Builds and returns a custom GSON converter factory for quotes.
     * @return Custom GSON converter factory for quotes
     */
    static GsonConverterFactory getGsonConverterFactory(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Quote.class, new QuoteJsonDeserializer());
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }

    /**
     * Prepares a Retrofit instance for interaction with the Zenquotes API.
     * @return Retrofit instance
     */
    static Retrofit getRetrofitInstance(){
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        return new Retrofit.Builder()
                .baseUrl(App.getContext().getResources().getString(R.string.zenquotes_url))
                .addConverterFactory(QuoteAPI.getGsonConverterFactory())
                .client(client)
                .build();
    }

    /**
     * Requests 50 random quotes.
     * @return A list of 50 random quotes
     */
    @GET("/api/quotes")
    Call<List<Quote>> getQuotes();
}
