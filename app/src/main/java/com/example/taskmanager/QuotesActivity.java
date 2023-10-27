package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.taskmanager.interfaces.QuoteAPI;
import com.example.taskmanager.quote.Quote;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class QuotesActivity extends AppCompatActivity {

    protected TextView linkTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);
        findViewById(R.id.goBackImageButton).setOnClickListener((view)->onBackPressed());
        linkTextView = findViewById(R.id.zenquotesLinkTextView);
        linkTextView.setText(
                Html.fromHtml(getString(R.string.zenquotes_link),
                        Html.FROM_HTML_MODE_COMPACT));

        Thread t = new Thread(()->{
            try {
                Retrofit retrofit = QuoteAPI.getRetrofitInstance();
                QuoteAPI api = retrofit.create(QuoteAPI.class);
                Call<List<Quote>> testCall = api.getQuotes();
                Response<List<Quote>> response = testCall.execute();

                Log.d("RESPONSE_CODE", Integer.toString(response.code()));
                assert response.body() != null;
                Log.d("RESPONSE_BODY", response.body().toString());
            }
            catch (Exception e) {
                Log.d("API_EXCEPTION", (e.getLocalizedMessage() == null)?
                        e.toString():e.getLocalizedMessage());
            }
        });
        t.start();
    }

    public void openZenquotesHandler(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.zenquotes_url)));
        startActivity(browserIntent);
    }
}