package com.example.taskmanager.quote;

public class Quote {
    private String quote = "";
    private String author = "";
    private String preformattedQuoteHTML = "";

    public Quote(String quote, String author) {
        this.quote = quote;
        this.author = author;
    }

    public Quote(String quote, String author, String preformattedQuoteHTML) {
        this(quote, author);
        this.preformattedQuoteHTML = preformattedQuoteHTML;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPreformattedQuoteHTML() {
        return preformattedQuoteHTML;
    }

    public void setPreformattedQuoteHTML(String preformattedQuoteHTML) {
        this.preformattedQuoteHTML = preformattedQuoteHTML;
    }

    @Override
    public String toString(){
        return String.format("\"%s\" - %s", quote, author);
    }
}
