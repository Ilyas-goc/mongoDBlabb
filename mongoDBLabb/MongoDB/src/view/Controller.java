package view;

import com.mongodb.MongoException;
import java.io.IOException;
import java.util.ArrayList;
import model.SearchMode;
import model.Book;
import model.BooksDbInterface;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.control.Alert.AlertType.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.Author;
import model.BookReviewed;

/*
 * The controller is responsible for handling user requests
 * (and in some cases the model).
 *
 * @author Baran Polat and Ilyas Göcmenoglu
 */
public class Controller {

    private final BooksPane booksView; // view
    private final BooksDbInterface booksDb; // model
    Book bookResult;
    Author authorResult;
    String isbn, title, columnName, name, newvalue;
    boolean isDone = false, isException = false, isConnect = false;

    List<Book> result = null;

    /**
     * We iniatialize view and model.
     */
    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;

    }

   
    /**
     * Connect to the DB via a "localhost" user
     */
    EventHandler<ActionEvent> connect = new EventHandler<ActionEvent>() {

        public void handle(ActionEvent e) {
            try {
                booksDb.connect(27017, "localhost", "", "myLib");
                booksView.errorAlert("Success", "Log in successful");

            } catch (Exception ex) {
                booksView.errorAlert("MongoException", "Access denied, wrong username or password");

            }

        }

    };

    /**
     * Disconnects from the DB and closes the program
     */
    EventHandler<ActionEvent> exit = new EventHandler<ActionEvent>() {

        public void handle(ActionEvent e) {
            try {
                boolean disconnected = booksDb.disconnect();

                System.out.println(disconnected);
                if (disconnected) {
                    booksView.errorAlert("", "Disconnected");

                    System.exit(0);
                }
            } catch (Exception ex) {
                booksView.errorAlert("Not even logged in", "You are not logged in, please use [x] button if not logged");
            }
        }

    };
    
    /**
     * Disconnects from the DB
     */
    EventHandler<ActionEvent> disconnect = new EventHandler<ActionEvent>() {

        public void handle(ActionEvent e) {
            try {
                booksView.clearBooks();
                boolean isDisconnected = booksDb.disconnect();
                if (isDisconnected) {
                    booksView.errorAlert("", "Disconnected");
                }
            } catch (NullPointerException ex) {

                booksView.errorAlert("Disco", "Log in first");
            }
        }

    };

    /**
     * Adds a book the Database with the Author included
     */
    protected void addBook(Book bookResult, Author authorResult) {
        this.bookResult = bookResult;
        this.authorResult = authorResult;

        new Thread() {
            @Override
            public void run() {
                try {
                    isDone = booksDb.addBook(authorResult, bookResult);
                } catch (IOException | MongoException e) {

                    e.printStackTrace();
                }
                javafx.application.Platform.runLater(
                        new Runnable() {
                    public void run() {

                        if (isDone) {
                            booksView.errorAlert("Succesful", "Book has been added");
                        } else {
                            booksView.errorAlert("Fail", "Book could not be added,maybe Author already exists...");
                        }

                    }

                });

            }
        }.start();
        isException = false;
        isDone = false;
    }

    /**
     * Updates a known book in the Database.
     */
    protected void updateBook(String tmpisbn, String tmptitle, String tmpcolumnName, String tmpnewValue) {

        this.isbn = tmpisbn;
        this.title = tmptitle;
        this.columnName = tmpcolumnName;
        this.newvalue = tmpnewValue;

        new Thread() {
            @Override
            public void run() {
                try {

                    isDone = booksDb.updateBook(title, isbn, columnName, newvalue);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (MongoException ex) {

                    ex.printStackTrace();
                }

                javafx.application.Platform.runLater(
                        new Runnable() {
                    public void run() {

                        if (isDone) {
                            booksView.errorAlert("Successful", "Book has been updated");
                        } else {
                            booksView.errorAlert("Fail", "Book could not be updated");
                        }

                    }

                });

            }
        }.start();
        isDone = false;
        isException = false;
    }

    /**
     * Removes a book from the Database.
     */
    protected void removeBooks(String title, String isbn) {
        this.title = title;
        this.isbn = isbn;

        new Thread() {
            @Override
            public void run() {
                try {
                    isDone = booksDb.removeBook(title, isbn);
                } catch (MongoException ex) {
                    ex.printStackTrace();
                }
                javafx.application.Platform.runLater(
                        new Runnable() {
                    public void run() {

                        if (isDone) {
                            booksView.errorAlert("Done", "It has been removed");
                        } else {
                            booksView.errorAlert("Fail", "Found no book to remove or transcation failed...");
                        }

                    }

                });

            }
        }.start();
        isDone = false;
        isException = false;
    }

    /**
     * Adds several authors to the book.
     */
    protected void addAuthorToBooks(String authorName, String bookName) {
        this.title = bookName;
        this.name = authorName;

        new Thread() {
            @Override
            public void run() {
                try {

                    isDone = booksDb.addAuthorToBook(name, title);

                }catch(MongoException ex){
                ex.printStackTrace();
                }catch (Exception e) {
                    isException = true;
                }
                javafx.application.Platform.runLater(
                        new Runnable() {
                    public void run() {
                        if (isException) {
                            booksView.errorAlert("Failure", "Author or book title is invalid...");
                        }
                        if (isDone) {
                            booksView.errorAlert("Successful", "Author has been added");
                        }
                    }

                });

            }
        }.start();
        isException = false;
        isDone = false;
    }

    /**
     * Connects to the database via the menuoptions in BookPane. Enter a
     * username and a password known in the database to access.
     */
    protected void loginScreenn(Optional<Pair<String, String>> result, String userName, String password) {

        result.ifPresent(usernamePassword -> {
            boolean isConnected = false;
            try {
                isConnected = booksDb.connect(27017, userName, password, "myLib");
                booksView.errorAlert("Success", "Log in successful");

                if (booksDb.getConnection() != null) {
                    booksDb.storeUser(userName);
                }
            } catch (IOException ex) {
               ex.printStackTrace();
            } catch (MongoException ex) {
                booksView.errorAlert("Mongoexception", "Access denied, wrong username or password");
            }finally{
                if(!isConnected){
                                   booksView.errorAlert("Mongoexception", "Access denied, wrong username or password");

                }
            }
           
        });
    }

    /**
     * Show reviews in the Database for the book selected.
     */
    protected ArrayList<BookReviewed> reviews(Book tmp) {
        ArrayList<BookReviewed> reviewTmp = new ArrayList<BookReviewed>();

        try {
            reviewTmp = booksDb.getReviews(tmp);

        } catch (MongoException ex) {

            booksView.errorAlert("Fail", "Failed to retrieve reviews...");
        }

        return reviewTmp;
    }

    /**
     * Review the selected book. You can only review one book per User.
     */
    protected void reviewBooks(Book tmp) {
        Alert alert = new Alert(AlertType.NONE);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Recension");
        alert.setHeaderText("Recensioner");

        Label label = new Label("Review:");

        TextArea textArea = new TextArea();
        textArea.setEditable(true);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE * 2);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        Button b = new Button("Lägg");

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        expContent.add(b, 0, 10);
        alert.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Boolean isAlreadyReviewed = false;
                try {

                    isAlreadyReviewed = booksDb.storeReview(tmp, booksDb.getCurrentUser(), textArea.getText());
                    textArea.setText("");
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MongoException ex) {
                    
                }
                if(isAlreadyReviewed){
                    booksView.errorAlert("Fail", "Cannot review same book twice");
                }else{
                    booksView.errorAlert("Done", "Book has been reviewed");
                }

            }
        });

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Searches for books in database through title,author,genre,rating and
     * isbn.
     *
     * @param searchFor Your search text
     * @param mode variable containing what search-method you have
     * chosen(title,author,genre,rating or isbn)
     */
    protected void onSearchSelectedThread(String searchFor, SearchMode mode) {

        new Thread() {

            @Override
            public void run() {
                try {

                    switch (mode) {
                        case Title:
                            result = booksDb.searchBooksByTitle(searchFor);

                            break;
                        case ISBN:
                            result = booksDb.searchBooksByISBN(searchFor);
                            break;
                        case Author:
                            result = booksDb.searchBooksByAuthor(searchFor);
                            break;
                        case Genre:
                            result = booksDb.searchBooksByGenre(searchFor);
                            break;
                        case Rating:
                            result = booksDb.searchBooksByRating(searchFor);
                            break;

                        default:

                    }
                    
                } catch(MongoException ex){
              
                ex.printStackTrace();
                }catch (Exception e) {

                    booksView.showAlertAndWait("Database error.", ERROR);
                }
                javafx.application.Platform.runLater(new Runnable() {
                    public void run() {
                        if (result.isEmpty()) {
                            booksView.showAlertAndWait("No books found", ERROR);

                        } else {

                            booksView.displayBooks(result);
                        }
                    }
                });

            }
        }.start();

    }
}
                        
