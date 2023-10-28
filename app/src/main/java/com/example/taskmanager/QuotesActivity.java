package com.example.taskmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.interfaces.QuoteAPI;
import com.example.taskmanager.quote.Quote;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class QuotesActivity extends AppCompatActivity {

    protected TextView linkTextView, quoteTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);
        findViewById(R.id.goBackImageButton).setOnClickListener((view)->onBackPressed());
        linkTextView = findViewById(R.id.zenquotesLinkTextView);
        quoteTextView = findViewById(R.id.quoteTextView);
        linkTextView.setText(
                Html.fromHtml(getString(R.string.zenquotes_link),
                        Html.FROM_HTML_MODE_COMPACT));
        getQuoteHandler(findViewById(R.id.getQuoteButton));
    }

    public void openZenquotesHandler(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.zenquotes_url)));
        startActivity(browserIntent);
    }

    public void getQuoteHandler(View v) {
        if(QuoteAPI.quotes.size() == 0) {
            Thread quoteFetchThread = new Thread(()->{
                try {
                    Retrofit retrofit = QuoteAPI.getRetrofitInstance();
                    QuoteAPI api = retrofit.create(QuoteAPI.class);
                    Call<List<Quote>> testCall = api.getQuotes();
                    Response<List<Quote>> response = testCall.execute();

                    if(response.body() != null) {
                        QuoteAPI.quotes.addAll((List<Quote>)response.body());
                        runOnUiThread(this::showQuote);
                    }

                }
                catch (Exception e) {
                    runOnUiThread(()-> Toast.makeText(getApplicationContext(),
                            getString(R.string.api_exception_message),
                            Toast.LENGTH_LONG).show());
                }
            });
            quoteFetchThread.start();
        }
        else {
            showQuote();
        }
    }

    private void showQuote() {
        if(QuoteAPI.quotes.size() > 0) {
            quoteTextView.setText(Html.fromHtml(QuoteAPI.quotes.remove(0).getPreformattedQuoteHTML(),
                    Html.FROM_HTML_MODE_COMPACT));
            quoteTextView.setVisibility(View.VISIBLE);
        }
    }
}