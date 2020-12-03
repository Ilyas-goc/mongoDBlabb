/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert.AlertType;

/**
 * A implementation of the BooksDBInterface interface to demonstrate how to use
 * it together with the user interface.
 *
 *
 *
 * @author baranp@kth.se
 */
public class MockBooksDb implements BooksDbInterface {

    private static Connection connect;
    private static String userName;
    private int cnt = 0;

    /**
     * Constructor
     */
    public MockBooksDb() {

    }

    /**
     * returns the current connection
     *
     * @return
     */
    @Override
    public Connection getConnection() {
        return this.connect;
    }

    /**
     * Connects to the database
     *
     * @param database the information for what database to connect to
     * @param username username at login
     * @param password password at login returns true if connected
     * @return
     */
    @Override
    public boolean connect(String database, String username, String password) throws IOException, SQLException {
        boolean isConnect = false;
        System.out.println(cnt);
        
        
            if (cnt == 0) {
                database = "jdbc:mysql://localhost:3306/bookauthordb";
                connect = DriverManager.getConnection(database, username, password);
                isConnect = true;
                
                cnt = 1;
               
            }
//            if (!connect.isValid(2)) {
//                database = "jdbc:mysql://localhost:3306/bookauthordb";
//                connect = DriverManager.getConnection(database, username, password);
//                isConnect = true;
//                System.out.println("Connected"); 
//                
//            } 
            else {System.out.println("Hhaa");
                isConnect = false;
                
            }
        
        
        
        return isConnect;

    }

    /**
     * Disconnects from the DB and exits the system.
     */
    @Override
    public boolean disconnect() throws SQLException {
        if (connect.isValid(2)) {
            connect.close();
            cnt=0;
            if (connect.isClosed()) {
               
                
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @returns the name of the current logged in user
     */
    @Override
    public String getCurrentUser() {
        return this.userName;
    }

    /**
     * Returns the reviews of a specific book
     *
     * @param con contains the current connection
     * @param tmp contains the book selected for reviews
     * @returns the reviews of that book (tmp)
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public ArrayList<BookReviewed> getReviews(Connection con, Book tmp) throws SQLException {
        String STRINGtmp1, STRINGtmp2;
        Date DATEtmp;
        ArrayList<BookReviewed> result = new ArrayList<>();

       
        Statement getReviews = null;
        try {
            // Execute the SQL statement

            getReviews = con.createStatement();
            ResultSet rs = getReviews.executeQuery("CALL getReviews('" + tmp.getTitle() + "');");

            while (rs.next()) {
               

                STRINGtmp1 = rs.getString(1);
                STRINGtmp2 = rs.getString(2);
               
                DATEtmp = rs.getDate(3);

                result.add(new BookReviewed(STRINGtmp1, STRINGtmp2, DATEtmp));

            }

        } finally {
            if (getReviews != null) {
                getReviews.close();
            }
        }
        return result;

    }

    /**
     * Stores the name of the current user in the database
     *
     * @param con contains the current connection
     * @param username username of the user
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public void storeUser(Connection con, String username) throws IOException, SQLException {
        Statement storeUser = null;
        userName = username;
        try {
            // Execute the SQL statement
            storeUser = con.createStatement();
            storeUser.executeUpdate("INSERT INTO bookauthordb.reviewer (rev_Name) VALUES ('" + username + "');");

        } catch (SQLException e) {
          
        } finally {
            if (storeUser != null) {
                storeUser.close();
            }
        }
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param con contains the current connection
     * @param searchTitle the name of the book to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public List<Book> searchBooksByTitle(Connection con, String searchTitle) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        String queryText = "SELECT * FROM book WHERE title LIKE '%" + searchTitle + "%';";
        result = executeSearchQuery(con, queryText);
        return result;

    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param con contains the current connection
     * @param searchAuthor the name of the author that has written the books
     * @return the result of the search
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public List<Book> searchBooksByAuthor(Connection con, String searchAuthor) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        String queryText = "SELECT * FROM book WHERE ISBN IN "
                + "(SELECT ISBN FROM wrote WHERE authorID IN "
                + "(SELECT authorID FROM author WHERE name ='" + searchAuthor + "'));";
        result = executeSearchQuery(con, queryText);
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param con contains the current connection
     * @param searchIsbn the ISBN of the book that is to be sought for
     * @returns the result of the search
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public List<Book> searchBooksByISBN(Connection con, String searchIsbn) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        String queryText = "SELECT * FROM book WHERE isbn='" + searchIsbn + "'";
        result = executeSearchQuery(con, queryText);
        return result;

    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param con contains the current current connection
     * @param searchGenre the genre of the books that is to be sought for
     * @returns the result of the search
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public List<Book> searchBooksByGenre(Connection con, String searchGenre) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        String queryText = "SELECT * FROM book WHERE genre='" + searchGenre + "'";
        result = executeSearchQuery(con, queryText);
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param con contains the current connection
     * @param searchRating the rating of the books that is to be sought for
     * @returns the result of the search
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public List<Book> searchBooksByRating(Connection con, String searchRating) throws IOException, SQLException {
        List<Book> result = new ArrayList<>();
        String queryText = "SELECT * FROM book WHERE rating='" + searchRating + "'";
        result = executeSearchQuery(con, queryText);
        return result;
    }

    /**
     * Adds an additional author to the specified book
     *
     * @param con contains the current connection
     * @param authorName name of the author that is to added to the book
     * @param bookName name of the book that will get the additional author
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public boolean addAuthorToBook(Connection con, String authorName, String bookName) throws SQLException {
        Statement addAuthorToBook = null;
        int checkIfDone = 0;
        boolean isDone = false;
        try {

            // Execute the SQL statement
            addAuthorToBook = con.createStatement();
            checkIfDone = addAuthorToBook.executeUpdate("CALL addAuthorToBook('" + authorName + "', '" + bookName + "');");
            

        } finally {
            if (addAuthorToBook != null) {
                addAuthorToBook.close();
            }
            if(checkIfDone>0){
                isDone = true;
            }
        }
        return isDone;
    }

    /**
     * Removes a given book
     *
     * @param con contains the current connection
     * @param bookName name of the book that will be removed
     * @param isbn isbn of the book that will be removed
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public boolean removeBook(Connection con, String bookName, String isbn) throws SQLException {
        int checkIfRemovedFromBook = 0, checkIfRemovedFromWrote = 0, checkIfRemovedFromReviews = 0;
        boolean removed = false;
        Statement deleteBookFromBookRows = null;
        Statement deleteBookFromWroteRows = null;
        Statement deleteBookFromReviewsRows = null;
        try {
            con.setAutoCommit(false);
            deleteBookFromWroteRows = con.createStatement();
            deleteBookFromReviewsRows = con.createStatement();
            deleteBookFromBookRows = con.createStatement();
            checkIfRemovedFromReviews = deleteBookFromReviewsRows.executeUpdate("DELETE FROM reviews WHERE ISBN= '" + isbn + "';");
            checkIfRemovedFromWrote = deleteBookFromWroteRows.executeUpdate("DELETE FROM wrote WHERE ISBN= '" + isbn + "';");
            checkIfRemovedFromBook = deleteBookFromBookRows.executeUpdate("DELETE FROM book WHERE title= '" + bookName + "' AND ISBN= '" + isbn + "';");

            con.commit();
        } catch (Exception e) {
           
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (deleteBookFromBookRows != null) {
                deleteBookFromBookRows.close();
            }
            if (deleteBookFromWroteRows != null) {
                deleteBookFromWroteRows.close();
            }
            if (deleteBookFromReviewsRows != null) {
                deleteBookFromReviewsRows.close();
            }
            if ((checkIfRemovedFromBook > 0 && checkIfRemovedFromWrote > 0) || (checkIfRemovedFromBook > 0 && checkIfRemovedFromWrote > 0 && checkIfRemovedFromReviews > 0)) {                      // kan lösas med att man returnar boolean mellan de sammanhängande metoderna

                removed = true;

            } 
            con.setAutoCommit(true);

        }

        return removed;
    } // Förkortning av dessa m designmönster

    /**
     * Updates a column in a specific book row
     *
     * @param con contains the current connection
     * @param bookName name of the book that will be updated
     * @param isbn isbn of the book that will updated
     * @param column name of the column that will be updated
     * @param newColumnValue new value for the selected column
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public boolean updateBook(Connection con, String bookName, String isbn, String column, String newColumnValue) throws IOException, SQLException {
        Statement updateBook = null;
        boolean isUpdated = false;
        int checkIfDone = 0;

        try {
            updateBook = con.createStatement();
          checkIfDone=  updateBook.executeUpdate("UPDATE book SET " + column + "= '" + newColumnValue + "'"
                    + "WHERE title='" + bookName + "' AND ISBN= '" + isbn + "';");

        } finally {
            if (updateBook != null) {
                updateBook.close();
                
            }
            if(checkIfDone>0){
                isUpdated = true;
            }

        }
        return isUpdated;
    }

    /**
     * Adds a new book with it's author to the mysql database
     *
     * @param con contains the current connection
     * @param author The instance of the author class which is to be added as
     * the author of the book
     * @param book The instance of the book class which is to be added as the
     * book
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public boolean addBook(Connection con, Author author, Book book) throws IOException, SQLException {
        Statement addBookWithNewAuthor = null;
        int checkIfDone = 0;
        boolean isAdded= false;

        try {
            addBookWithNewAuthor = con.createStatement();
            checkIfDone = addBookWithNewAuthor.executeUpdate("CALL addBook('" + book.getIsbn() + "', '" + book.getTitle() + "', '"
                    + book.getGenre() + "', " + book.getRating() + ", '"
                    + author.getName() + "', '" + author.getDob() + "');");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (addBookWithNewAuthor != null) {
                addBookWithNewAuthor.close();
            }
            if(checkIfDone>0){
                isAdded = true;
            }
           
        }
        return isAdded;
    }

    /**
     * Stores the review on a specific book on the mysql database
     *
     * @param con Contains the current connection
     * @param tmp The book that has the review
     * @param userName Who the reviewer is
     * @param reviewText Contains the review text
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public void storeReview(Connection con, Book tmp, String userName, String reviewText) throws IOException, SQLException {
        Statement storeReview = null;
        try {
            // Execute the SQL statement
            storeReview = con.createStatement();
            storeReview.executeUpdate("INSERT INTO bookauthordb.reviews(rev_Name,ISBN,reviewText,review_Date )"
                    + "VALUE ('" + userName + "', (SELECT ISBN FROM book WHERE title = '" + tmp.getTitle() + "'), '" + reviewText + "', '" + LocalDate.now() + "');");

        } finally {
            if (storeReview != null) {
                storeReview.close();
            }

        }

    }

    /**
     * Exectures the Select query
     *
     * @param con contains the current connection
     * @param queryText The syntax of the select query
     * @return return the books that was sought for
     * @throws SQLException
     * @throws SQLException
     */
    @Override
    public List<Book> executeSearchQuery(Connection con, String queryText) throws SQLException, SQLException {

        String STRINGtmp1, STRINGtmp2, STRINGtmp3;
        int INTtmp;
        List<Book> result = new ArrayList<>();
        // mock implementation
        // NB! Your implementation should select the books matching
        // the search string via a query with to a database.
        Statement executeSearch = null;
        try {
            // Execute the SQL statement
            executeSearch = con.createStatement();
            ResultSet rs = executeSearch.executeQuery(queryText);

            // Get the attribute values
            while (rs.next()) {
                // NB! This is an example, -not- the preferred way to retrieve data.
                // You should use methods that return a specific data type, like
                // rs.getInt(), rs.getString() or such.
                // It's also advisable to store each tuple (row) in an object of
                // custom type (e.g. Employee).

                STRINGtmp1 = rs.getString(1);
                STRINGtmp2 = rs.getString(2);
                STRINGtmp3 = rs.getString(3);
                INTtmp = rs.getInt(4);

                result.add(new Book(STRINGtmp1, STRINGtmp2, INTtmp, Genre.valueOf(STRINGtmp3), new ArrayList<BookReviewed>()));
            }

        } finally {
            

        }
        return result;

    }

    
    

}
