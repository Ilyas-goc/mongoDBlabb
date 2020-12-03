package model;

import java.sql.Date;

/**
 * The class representing the book that has been reviewed
 *
 * @author 46762
 */
public class BookReviewed {

    private String reviewerName, reviewText;
    private Date date;

    /**
     * Constructor
     *
     * @param reviewerName name of the reviewer
     * @param reviewText the review text
     * @param date date of the review text
     */
    public BookReviewed(String reviewerName, String reviewText, Date date) {
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.date = date;
    }

    /**
     *
     * @returns the reviewer name
     */
    public String getReviewerName() {
        return this.reviewerName;
    }

    /**
     *
     * @returns the review text
     */
    public String getReviewText() {
        return this.reviewText;
    }

    /**
     *
     * @returns the date of the review
     */
    public Date getDate() {
        Date tmp = this.date;
        return tmp;
    }

    @Override
    public String toString() {
        String info = "Reviewer name: " + this.getReviewerName() + "\nReview text: " + this.getReviewText()
                + "\nReview date:" + this.getDate();
        return info;
    }
}
