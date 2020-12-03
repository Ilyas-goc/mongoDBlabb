package model;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * This interface declares methods for querying a Books database. Different
 * implementations of this interface handles the connection and queries to a
 * specific DBMS and database, for example a MySQL or a MongoDB database.
 *
 * @author baranp@kth.se, figo@kth.se
 */
public class MongoDb implements BooksDbInterface {

    private int cnt = 0;
    private static MongoClient connect;
    private static MongoDatabase database;

    private static String userName;

    /**
     * Connects to the database
     *
     * @param portNmr
     * @param tmpDatabase the information for what database to connect to
     * @param username username at login
     * @param password password at login returns true if connected
     * @return true if connected
     * @throws java.io.IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public boolean connect(int portNmr, String username, String password, String tmpDatabase) throws IOException, MongoException {

        System.out.println(checkConnection());
        if (checkConnection()) {
            System.out.println(checkConnection());
            return false;
        }

        if (!checkConnection()) {
            System.out.println(checkConnection());
            Builder o = MongoClientOptions.builder().serverSelectionTimeout(1500);
            System.out.println(cnt);
            MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, tmpDatabase,
                    password.toCharArray());
            connect = new MongoClient(new ServerAddress("localhost", 27017), Arrays.asList(mongoCredential), o.build());
        }
        try {
            connect.getAddress();
            database = connect.getDatabase(tmpDatabase);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Disconnects from the DB and exits the system.
     *
     * @throws NullPointerException
     * @return true if disconnected
     */
    @Override
    public boolean disconnect() throws NullPointerException {

        connect.close();

        return true;
    }

    /**
     * Stores the name of the current user in the database
     *
     * @param username username of the user
     * @throws IOException
     * @throws NullPointerException
     */
    @Override
    public void storeUser(String username) throws IOException, MongoException {

        MongoCollection collection = database.getCollection("reviewer");
        try {

            userName = username;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    /**
     * Stores the review on a specific book on the mysql database
     *
     * @param tmp The book that has the review
     * @param userName Who the reviewer is
     * @param reviewText Contains the review text
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public Boolean storeReview(Book tmp, String userName, String reviewText) throws IOException, MongoException {
        Boolean isAlreadyReviewed = false;
        int i = 0;

        MongoCollection collection = database.getCollection("book");
        MongoCollection counterCollection = database.getCollection("reviewCounter");

        Document foundBook = (Document) collection.find(eq("Title", tmp.getTitle())).first();

        ArrayList<Document> arrayOfReviews = new ArrayList<>();
        arrayOfReviews.addAll((ArrayList<Document>) foundBook.get("Reviews"));

        for (int k = 0; k < arrayOfReviews.size(); k++) {

            String revName = String.valueOf(arrayOfReviews.get(k).get("name"));

            if (revName.equals(this.getCurrentUser())) {
                isAlreadyReviewed = true;
            }

        }
        boolean foundReviewer = false;
        if (!isAlreadyReviewed) {

            String queryForReviewers = "Reviews.name";
            BasicDBObject result = new BasicDBObject(queryForReviewers, java.util.regex.Pattern.compile(this.getCurrentUser()));

            Document reviewerFound = (Document) collection.find(result).first();
            List<Document> arrayOfReviewers = new ArrayList<>();
            try {
                arrayOfReviewers.addAll((ArrayList<Document>) reviewerFound.get("Reviews"));

                Iterator<Document> searchForReviewer = arrayOfReviewers.iterator();

                while (searchForReviewer.hasNext()) {

                    if (searchForReviewer.next().get("name").equals(this.getCurrentUser())) {

                        foundReviewer = true;
                        System.out.println(i);
                        break;

                    }
                    i++;
                }
            } catch (NullPointerException e) {

            }

            if (!foundReviewer) {

                Bson seqValue = new Document("sequence_value", 1);
                Bson updateOperationn = new Document("$inc", seqValue);

                Document foundReviewerId = (Document) counterCollection.find(eq("_id", "reviewerid")).first();
                Document getOldSeqValue = (Document) counterCollection.findOneAndUpdate(foundReviewerId, updateOperationn);

                Bson updatedValue = new Document("Reviews", new BasicDBObject("reviewerId", getOldSeqValue.get("sequence_value")).append("name", this.getCurrentUser()).append("Reviewtext", reviewText));

                Bson updateOperation = new Document("$push", updatedValue);
                collection.updateOne(foundBook, updateOperation);
            } else {

                Bson updatedValue = new Document("Reviews", new BasicDBObject("reviewerId", arrayOfReviewers.get(i).get("reviewerId")).append("name", this.getCurrentUser()).append("Reviewtext", reviewText));

                Bson updateOperation = new Document("$push", updatedValue);
                collection.updateOne(foundBook, updateOperation);
            }
        }

        return isAlreadyReviewed;
    }

    /**
     * Returns the reviews of a specific book
     *
     * @param tmp contains the book selected for reviews
     * @return the reviews of that book (tmp)
     * @throws com.mongodb.MongoException
     */
    @Override
    public ArrayList<BookReviewed> getReviews(Book tmp) throws MongoException {

        MongoCollection<Document> collection = database.getCollection("book"); // lite onödigt med att göra connect helatiden, man kanske gör det som en global variablet
        ArrayList<BookReviewed> result = new ArrayList<>();

        try {
            long millis = System.currentTimeMillis();
            java.sql.Date date = new java.sql.Date(millis);

            FindIterable find = collection.find(eq("Title", tmp.getTitle()));

            for (MongoCursor<Document> cursor = find.iterator(); cursor.hasNext();) {
                Document doc = cursor.next();
                ArrayList<Document> arrayOfReviews = new ArrayList<>();
                arrayOfReviews.addAll((ArrayList<Document>) doc.get("Reviews"));
                for (int i = 0; i < arrayOfReviews.size(); i++) {

                    String revName = String.valueOf(arrayOfReviews.get(i).get("name"));
                    String revText = String.valueOf(arrayOfReviews.get(i).get("Reviewtext"));
                    result.add(new BookReviewed(revName, revText, date));

                }
            }

        } finally {

        }
        return result;
    }

    /**
     * Exectures the Select query
     *
     * @param searchType contains the search method
     * @param queryText What to search for
     * @return return the books that was sought for
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> executeSearchQuery(String searchType, String searchValue) throws IOException, MongoException {

        List<Book> result = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("book");

        if (searchType.equals("Rating")) {
            int intSearchValue = Integer.parseInt(searchValue);
            FindIterable find = collection.find(eq(searchType, intSearchValue));
            for (MongoCursor<Document> cursor = find.iterator(); cursor.hasNext();) {
                Document doc = cursor.next();

                result.add(new Book((String) doc.get("_id"), (String) doc.get("Title"), (Integer) doc.get("Rating"), Genre.valueOf((String) doc.get("Genre")), new ArrayList<BookReviewed>()));

            }
        } else {
            FindIterable find = collection.find(eq(searchType, searchValue));
            for (MongoCursor<Document> cursor = find.iterator(); cursor.hasNext();) {
                Document doc = cursor.next();

                result.add(new Book((String) doc.get("_id"), (String) doc.get("Title"), (Integer) doc.get("Rating"), Genre.valueOf((String) doc.get("Genre")), new ArrayList<BookReviewed>()));

            }
        }

        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param searchTitle the name of the book to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws IOException, MongoException {
        String searchType = "Title";
        List<Book> result = new ArrayList<>();
        result = executeSearchQuery(searchType, searchTitle);
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param searchIsbn the ISBN of the book that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> searchBooksByISBN(String searchIsbn) throws IOException, MongoException {
        String searchType = "Isbn";
        List<Book> result = new ArrayList<>();
        result = executeSearchQuery(searchType, searchIsbn);
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param searchAuthor the name of the author that has written the books
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> searchBooksByAuthor(String searchAuthor) throws IOException, MongoException {
        String searchType = "Authors.name";
        BasicDBObject query = new BasicDBObject(searchType, java.util.regex.Pattern.compile(searchAuthor));

        List<Book> result = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection("book");

        try {

            FindIterable find = collection.find(query);

            for (MongoCursor<Document> cursor = find.iterator(); cursor.hasNext();) {
                Document doc = cursor.next();

                result.add(new Book((String) doc.get("Isbn"), (String) doc.get("title"), (Integer) doc.get("Rating"), Genre.valueOf((String) doc.get("Genre")), new ArrayList<BookReviewed>()));

            }

        } finally {

        }
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param searchGenre the genre of the books that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> searchBooksByGenre(String searchGenre) throws IOException, MongoException {
        String searchType = "Genre";
        List<Book> result = new ArrayList<>();
        result = executeSearchQuery(searchType, searchGenre);
        return result;
    }

    /**
     * Returns the books obtained from the Select query
     *
     * @param searchRating the rating of the books that is to be sought for
     * @return the result of the search
     * @throws IOException
     * @throws com.mongodb.MongoException
     */
    @Override
    public List<Book> searchBooksByRating(String searchRating) throws IOException, MongoException {
        String searchType = "Rating";
        List<Book> result = new ArrayList<>();
        result = executeSearchQuery(searchType, searchRating);
        return result;
    }

    /**
     * Adds an additional author to the specified book
     *
     * @param authorName name of the author that is to added to the book
     * @param bookName name of the book that will get the additional author
     * @throws com.mongodb.MongoException
     */
    @Override
    public boolean addAuthorToBook(String authorName, String bookName) throws MongoException {
        boolean isUpdated = false;

        MongoCollection collection = database.getCollection("book");

        Author author = null;

        Document found = (Document) collection.find(eq("Title", bookName)).first();

        String queryForAuthors = "Authors.name";
        BasicDBObject result = new BasicDBObject(queryForAuthors, java.util.regex.Pattern.compile(authorName));

        Document authorFounds = (Document) collection.find(result).first();
        List<Document> arrayOfAuthors = new ArrayList<>();
        arrayOfAuthors.addAll((ArrayList<Document>) authorFounds.get("Authors"));

        Iterator<Document> searchForAuthor = arrayOfAuthors.iterator();
        String foundAuthorName = null;
        java.sql.Date dOb = null;
        int i = 0;
        while (searchForAuthor.hasNext()) {

            if (searchForAuthor.next().get("name").equals(authorName)) {
                foundAuthorName = String.valueOf(arrayOfAuthors.get(i).get("name"));
                dOb = java.sql.Date.valueOf(String.valueOf(arrayOfAuthors.get(i).get("Date of birth")));
                break;

            }
            i++;
        }

        author = new Author(foundAuthorName, new ArrayList<Book>(), dOb);

        Bson updatedValue = new Document("Authors", new BasicDBObject("authorId", arrayOfAuthors.get(i).get("authorId")).append("name", author.getName()).append("Date of birth", "" + author.getDob() + ""));
        Bson updateOperation = new Document("$addToSet", updatedValue);
        collection.updateOne(found, updateOperation);

        isUpdated = true;

        return isUpdated;
    }

    /**
     * Removes a given book
     *
     * @param bookName name of the book that will be removed
     * @param isbn isbn of the book that will be removed
     * @throws com.mongodb.MongoException
     */
    @Override
    public boolean removeBook(String bookName, String isbn) throws MongoException {
        boolean isRemoved = false;

        MongoCollection collection = database.getCollection("book");

        if (collection.findOneAndDelete(and(eq("Title", bookName), eq("_id", isbn))) != null) {

            isRemoved = true;
        }

        return isRemoved;
    }

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
    @Override
    public boolean updateBook(String bookName, String isbn, String column, String newColumnValue) throws IOException, MongoException {
        boolean isUpdated = false;

        MongoCollection collection = database.getCollection("book");

        Document found = (Document) collection.find(and(eq("Title", bookName), eq("_id", isbn))).first();
        if (column != "Rating") {

            if (found != null) {
                Bson updatedValue = new Document(column, newColumnValue);
                Bson updateOperation = new Document("$set", updatedValue);
                collection.updateOne(found, updateOperation);

                isUpdated = true;
            }
        } else {

            if (found != null) {
                int intNewColumnValue = Integer.parseInt(newColumnValue);
                Bson updatedValue = new Document(column, intNewColumnValue);
                Bson updateOperation = new Document("$set", updatedValue);
                collection.updateOne(found, updateOperation);

                isUpdated = true;
            }

        }

        return isUpdated;
    }

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
    @Override
    public boolean addBook(Author author, Book book) throws IOException, MongoException {

        boolean isAdded = false, foundAuthor = false;
        MongoCollection collection = database.getCollection("book");
        MongoCollection counterCollection = database.getCollection("authorCounter");

        List<Document> reviewsArray = new ArrayList<Document>();
        List<Document> AuthorsArray = new ArrayList<Document>();

        String queryForAuthors = "Authors.name";
        BasicDBObject result = new BasicDBObject(queryForAuthors, java.util.regex.Pattern.compile(author.getName()));
        Document authorFounds = (Document) collection.find(result).first();
        List<Document> arrayOfAuthors = new ArrayList<>();
        try {
            arrayOfAuthors.addAll((ArrayList<Document>) authorFounds.get("Authors"));

            Iterator<Document> searchForAuthor = arrayOfAuthors.iterator();
            String foundAuthorid = null;

            int i = 0;
            while (searchForAuthor.hasNext()) {

                if (searchForAuthor.next().get("name").equals(author.getName())) {
                    foundAuthorid = String.valueOf(arrayOfAuthors.get(i).get("name"));
                    System.out.println(foundAuthorid);
                    foundAuthor = true;
                    break;

                }
                i++;
            }
        } catch (NullPointerException e) {

        }
        Bson seqValue = new Document("sequence_value", 1);
        Bson updateOperation = new Document("$inc", seqValue);

        Document found = (Document) counterCollection.find(eq("_id", "authorid")).first();
        Document getOldSeqValue = (Document) counterCollection.findOneAndUpdate(found, updateOperation);

        AuthorsArray.add(new Document("authorId", getOldSeqValue.get("sequence_value")).append("name", author.getName()).append("Date of birth", "" + author.getDob() + ""));

        Document newBook = new Document("Title", book.getTitle())
                .append("Genre", "" + book.getGenre() + "")
                .append("Rating", book.getRating())
                .append("_id", book.getIsbn())
                .append("Authors", AuthorsArray)
                .append("Reviews", reviewsArray);
        collection.insertOne(newBook);

        isAdded = true;

        return isAdded;
    }

    /**
     * returns the current connection
     *
     * @return
     */
    @Override
    public MongoClient getConnection() {
        return this.connect;
    }

    /**
     *
     * @return the name of the current logged in user
     */
    @Override
    public String getCurrentUser() {
        return this.userName;
    }

    /**
     * Checks if there is an active connection
     *
     * @return true if there is one
     */
    @Override
    public boolean checkConnection() {
        try {
            connect.getAddress();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
