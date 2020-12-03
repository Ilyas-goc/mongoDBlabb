package model;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Class of an author
 *
 * @author 46762
 */
public class Author {

    private String name;
    private ArrayList<Book> books;
    private Date dob;

    /**
     * Constructor
     *
     * @param name of the authot
     * @param books that the author has written
     * @param dob_ date of birth
     */
    public Author(String name, ArrayList<Book> books, Date dob_) {

        this.name = name;
        this.dob = dob_;
        this.books = new ArrayList<>();

    }

    /**
     *
     * @returns the name of the author
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds a new book that this author has written
     *
     * @param b
     */
    public void addBook(Book b) {
        this.books.add(b);
    }

    /**
     * @return the date of birth
     */
    public Date getDob() {
        return dob;
    }

    @Override
    public String toString() {
        String info = "Name" + this.getName();

        return info;
    }

}
