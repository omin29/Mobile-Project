package com.example.taskmanager.quote;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class QuoteJsonDeserializer implements JsonDeserializer<Quote> {
    //https://docs.zenquotes.io/zenquotes-documentation/#response
    @Override
    public Quote deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String quote = jsonObject.get("q").getAsString();
        String author = jsonObject.get("a").getAsString();
        String preformattedQuoteHTML = jsonObject.get("h").getAsString();

        return new Quote(quote, author, preformattedQuoteHTML);
    }
}
