package com.psikolojikdanismanlik.randevusistemi.dto.request;

public class FeedbackRequestDto {
    private String comment;
    private int rating;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
