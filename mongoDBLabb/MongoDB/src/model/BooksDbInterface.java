package model;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface declares methods for querying a Books database. Different
 * implementations of this interface handles the connection and queries to a
 * specific DBMS and database, for example a MySQL or a MongoDB database.
 *
 * @author baranp@kth.se
 */
public interface BooksDbInterface {

    /**
     * Connects to the database
     *
     * @param portNmr
     * @param database the information for what database to connect to
     * @param username username at login
     * @param password password at login returns true if connected
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.mongodb.MongoException
     */
    public boolean connect(int portNmr, String username, String password, String database) throws IOException, MongoException;

    ;
    
   /**
     * Disconnects from the DB and exits the system.
     * @throws NullPointerException
     * @return true if disconnected
     */
    public boolean disconnect() throws NullPointerException;

    /**
     * Stores the name of the current user in the database
     *
     * @param username username of the user
     * @throws IOException
     * @throws NullPointerException
     */
    public void storeUser(String username) throws IOException, MongoException;

    ;
   
    /**
     * Stores the review on a specific book on the mysql database
     *
     * @param tmp The book that has the review
     * @param userName Who the reviewer is
     * @param reviewText Contains the review text
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public Boolean storeReview(Book tmp, String userName, String reviewText) throws IOException, MongoException;

    ;
    
    /**
     * Returns the reviews of a specific book
     *
     * @param tmp contains the book selected for reviews
     * @return the reviews of that book (tmp)
     * @throws com.mongodb.MongoException
     */
    public ArrayList<BookReviewed> getReviews(Book tmp) throws MongoException;

    ;
    
     /**
     * Exectures the Select query
     *
     * @param searchType contains the search method
     * @param queryText What to search for
     * @return return the books that was sought for
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> executeSearchQuery(String searchType, String queryText) throws IOException, MongoException;

    ;
    
    /**
     * Returns the books obtained from the Select query
     *
     * @param searchTitle the name of the book to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> searchBooksByTitle(String searchTitle) throws IOException, MongoException;

    ;
    
     /**
     * Returns the books obtained from the Select query
     *
     * @param searchIsbn the ISBN of the book that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> searchBooksByISBN(String searchIsbn) throws IOException, MongoException;

    ;
       
    /**
     * Returns the books obtained from the Select query
     *
     * @param searchAuthor the name of the author that has written the books
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, MongoException;

    ;
    
    /**
     * Returns the books obtained from the Select query
     *
     * @param searchGenre the genre of the books that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, MongoException;

    ;
    
    /**
     * Returns the books obtained from the Select query
     *
     * @param searchRating the rating of the books that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public List<Book> searchBooksByRating(String searchRating) throws IOException, MongoException;

    ;
    
    /**
     * Adds an additional author to the specified book
     *
     * @param authorName name of the author that is to added to the book
     * @param bookName name of the book that will get the additional author
     * @throws com.mongodb.MongoException
     */
    public boolean addAuthorToBook(String authorName, String bookName) throws MongoException;

    ;
    
    /**
     * Removes a given book
     *
     * @param bookName name of the book that will be removed
     * @param isbn isbn of the book that will be removed
     * @throws com.mongodb.MongoException
     */
    public boolean removeBook(String bookName, String isbn) throws MongoException;

    ;
    
    /**
     * Updates a column in a specific book row
     *
     * @param bookName name of the book that will be updated
     * @param isbn isbn of the book that will updated
     * @param column name of the column that will be updated
     * @param newColumnValue new value for the selected column
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public boolean updateBook(String bookName, String isbn, String column, String newColumnValue) throws IOException, MongoException;

    ;
    
    /**
     * Adds a new book with it's author to the mysql database
     *
     * @param author The instance of the author class which is to be added as
     * the author of the book
     * @param book The instance of the book class which is to be added as the
     * book
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    public boolean addBook(Author author, Book book) throws IOException, MongoException;

    ;
    
    /**
     * returns the current connection
     *
     * @return
     */
    public MongoClient getConnection();

    ;
    
     /**
     *
     * @return the name of the current logged in user
     */
    public String getCurrentUser();

    ;
    
     /**
     * Checks if there is an active connection
     * @return true if there is one
     */
    public boolean checkConnection();
;


}
