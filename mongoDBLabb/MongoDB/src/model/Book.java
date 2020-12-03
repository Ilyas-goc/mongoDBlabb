package model;

import java.util.ArrayList;

/**
 * Representation of a book.
 *
 * @author anderslm@kth.se
 */
public class Book {

    private String isbn;
    private String title;
    private int[] rating = {1, 2, 3, 4, 5};
    private int bookRating;

    private String storyLine = "";
    private Genre genre;
    private ArrayList<Author> authors;
    private Author author;
    private ArrayList<BookReviewed> listOfBookTexts;

    /**
     * Constructor
     *
     * @param isbn of the book
     * @param title of the book
     * @param ratingChoice the rating of the book
     * @param genre of the book
     * @param listOfBookTexts list of review texts on this book
     */
    public Book(String isbn, String title, int ratingChoice, Genre genre, ArrayList<BookReviewed> listOfBookTexts) {
        this.authors = new ArrayList<Author>();
        this.authors.add(author);
        this.isbn = isbn;
        this.title = title;

        this.genre = genre;
        this.bookRating = rating[ratingChoice - 1];
        this.listOfBookTexts = listOfBookTexts;
    }

    /**
     *
     * @param tmp
     * @param isbn
     * @param title
     * @param ratingChoice
     * @param genre
     * @param listOfBookTexts
     */
    public Book(String tmp, String isbn, String title, int ratingChoice, Genre genre, ArrayList<BookReviewed> listOfBookTexts) {
        this(isbn, title, ratingChoice, genre, listOfBookTexts);
    }

    /**
     * Adds a author to this book
     *
     * @param a is an author instance ( meaning the author)
     */
    public void addAuthor(Author a) {
        this.authors.add(a);
    }

    /**
     *
     * @returns the first author of the book
     */
    public Author getFirstAuthor() {
        Author tmp = this.authors.get(0);
        return tmp;
    }

    /**
     *
     * @returns the review texts of this book
     */
    public ArrayList<BookReviewed> getBookTexts() {
        ArrayList<BookReviewed> tmp = new ArrayList<BookReviewed>();
        for (int i = 0; i < listOfBookTexts.size(); i++) {
            tmp.add(listOfBookTexts.get(i));

        }
        return tmp;
    }

    /**
     *
     * @returns the rating
     */
    public int getRating() {
        int tmp = this.bookRating;
        return tmp;
    }

    /**
     *
     * @returns the genre
     */
    public Genre getGenre() {
        Genre tmp = this.genre;
        return tmp;
    }

    /**
     *
     * @returns the isbn
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     *
     * @returns the title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @returns the story line
     */
    public String getStoryLine() {
        return storyLine;
    }

    /**
     * This method searches for an author that has written this book, if the
     * author is found, true is returned
     *
     * @param authorName name of the author to be sought for
     * @returns boolean value
     */
    public Boolean searchForAuthor(String authorName) {
        Boolean flag = false;
        for (int i = 0; i < authors.size(); i++) {
            if (authors.get(i).getName().contains(authorName)) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * Sets the story line of this book
     *
     * @param storyLine text of storyline
     */
    public void setStoryLine(String storyLine) {
        this.storyLine = storyLine;
    }

    @Override
    public String toString() {
        return this.getTitle() + ", " + this.getIsbn() + ", " + this.getGenre() + ", " + this.getRating();
    }
}
