package com.nanodegree.tkamat.popularmovies.reviews;

/**
 * Created by tnadkarn on 11/13/2017.
 */

public class ReviewData {
    private String content;
    private String author;
    private String url;

    public ReviewData(String content, String author, String url)
    {
        this.content = content;
        this.author = author;
        this.url = url;
    }

    public String getContent()
    {
        return content;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getUrl()
    {
        return url;
    }
}
