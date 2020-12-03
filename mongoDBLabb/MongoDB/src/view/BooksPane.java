package view;

import model.SearchMode;
import model.Book;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.Author;
import model.BookReviewed;

import model.Genre;
import model.MongoDb;

/**
 * The main pane for the view, extending VBox and including the menus. An
 * internal BorderPane holds the TableView for books and a search utility.
 *
 * @author anderslm@kth.se
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable; // the data backing the table view
    private final TextField titleField = new TextField();
    private final TextField isbnField = new TextField();
    private final ComboBox<model.Genre> genreChoice = new ComboBox(FXCollections
            .observableArrayList(model.Genre.values()));
    private final ComboBox<Integer> ratingChoice = new ComboBox(FXCollections
            .observableArrayList(1, 2, 3, 4, 5));
    private final TextField authorNameField = new TextField();
    private final TextField bookNameField = new TextField();
    private final TextField newValueField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField usernameField = new TextField();
    PasswordField passwordField = new PasswordField();
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button showReviewButton, addReviewButton, searchButton;
    DatePicker dob = new DatePicker();
    private final ComboBox<String> columnField = new ComboBox(FXCollections
            .observableArrayList("Title", "Genre", "Rating"));

    Controller controller;
    private static int index;

    private MenuBar menuBar;

    public BooksPane(MongoDb booksDb) {
        this.controller = new Controller(booksDb, this);
        this.init(controller);
    }

    /**
     * Display a new set of books, e.g. from a database select, in the
     * booksTable table view.
     *
     * @param books the books to display
     */
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);

    }

    public void clearBooks() {
        booksInTable.clear();
    }

    /**
     * Notify user on input error or exceptions.
     *
     * @param msg the message
     * @param type types: INFORMATION, WARNING et c.
     */
    protected void showAlertAndWait(String msg, Alert.AlertType type) {
        // types: INFORMATION, WARNING et c.
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
        
    }

    private void init(Controller controller) {

        booksInTable = FXCollections.observableArrayList();

        // init views and event handlers
        initBooksTable();
        initSearchView(controller);
        initMenus();

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton, showReviewButton, addReviewButton);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false); // don't allow user updates (yet)

        // define columns
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");

        booksTable.getColumns().addAll(titleCol, isbnCol, ratingCol, genreCol);
        // give title column some extra space
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.5));

        // define how to fill data for each cell, 
        // get values from Book properties
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // associate the table view with the data
        booksTable.setItems(booksInTable);
    }

    private void initSearchView(Controller controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");
        showReviewButton = new Button("Show reviews");
        addReviewButton = new Button("Add review");

        // event handling (dispatch to controller)
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String searchFor = searchField.getText();
                SearchMode mode = searchModeBox.getValue();
                controller.onSearchSelectedThread(searchFor, mode);
            }
        });

        showReviewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int index = booksTable.getSelectionModel().getSelectedIndex();
                Book tmp = booksTable.getItems().get(index);
                frontEndReviews(tmp);
            }
        });

        addReviewButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                index = booksTable.getSelectionModel().getSelectedIndex();
                Book tmp = booksTable.getItems().get(index);
                controller.reviewBooks(tmp);
            }
        });
    }

    private void initMenus() {

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(controller.exit);
        MenuItem connectItem = new MenuItem("Connect to Db");
        connectItem.setOnAction(controller.connect);

        MenuItem loginItem = new MenuItem("Login");
        loginItem.setOnAction((ActionEvent e) -> {
            frontEndLoginScreen(controller);
        });

        MenuItem disconnectItem = new MenuItem("Disconnect");
        disconnectItem.setOnAction(controller.disconnect);
        fileMenu.getItems().addAll(exitItem, connectItem, loginItem, disconnectItem);

        Menu searchMenu = new Menu("Search");
        MenuItem titleItem = new MenuItem("Title");
        MenuItem isbnItem = new MenuItem("ISBN");
        MenuItem authorItem = new MenuItem("Author");
        searchMenu.getItems().addAll(titleItem, isbnItem, authorItem);

        Menu manageMenu = new Menu("Manage");

        MenuItem addItem = new MenuItem("Add book");
        addItem.setOnAction((ActionEvent e) -> {
            frontEndAddbook(controller);
        });

        MenuItem addAuthorToBook = new MenuItem("Add author to book");
        addAuthorToBook.setOnAction((ActionEvent e) -> {
            frontEndAddAuthorToBook(controller);
        });

        MenuItem removeItem = new MenuItem("Remove book");
        removeItem.setOnAction((ActionEvent e) -> {
            frontEndRemoveBook(controller);
        });

        MenuItem updateItem = new MenuItem("Update book");
        updateItem.setOnAction((ActionEvent e) -> {
            frontEndUpdateBook(controller);
        });

        manageMenu.getItems().addAll(addItem, addAuthorToBook, removeItem, updateItem);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, searchMenu, manageMenu);
    }

    public void frontEndAddbook(Controller controller) {

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add a new Book with new Author");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);
        grid.add(new Label("Isbn "), 1, 2);
        grid.add(isbnField, 2, 2);
        grid.add(new Label("Genre"), 1, 3);
        grid.add(genreChoice, 2, 3);
        grid.add(new Label("Rating"), 1, 4);
        grid.add(ratingChoice, 2, 4);
        grid.add(new Label("Author name:"), 1, 5);
        grid.add(authorNameField, 2, 5);
        grid.add(new Label("Date of Birth:"), 1, 6);
        grid.add(dob, 2, 6);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk
                = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel
                = new ButtonType("Go back", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter((ButtonType param) -> {
            String isbn = null;
            String title = null;
            int rating = 0;
            Genre genre = null;
            if (param == buttonTypeOk) {
                if (!validData()) {
                    BooksPane.errorAlert("Fail","Invalid inputs");

                }
                if (validData()) {
                    isbn = isbnField.getText();
                    title = titleField.getText();
                    rating = ratingChoice.getValue();
                    genre = genreChoice.getValue();
                    Author authorResult = new Author(authorNameField.getText(), new ArrayList<Book>(), Date.valueOf(dob.getValue()));
                    Book bookResult = new Book(
                            isbn,
                            title,
                            rating, genre,
                            new ArrayList<BookReviewed>());

                    controller.addBook(bookResult, authorResult);
                    clearAddBookData();

                    return bookResult;
                }
            }

            
            dialog.close();
            dialog.getDialogPane().getButtonTypes().clear();
            return null;
        });

        dialog.show();

    }

    public void frontEndUpdateBook(Controller controller) {
        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Update book");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Book name: "), 1, 1);
        grid.add(nameField, 2, 1);
        grid.add(new Label("ISBN: "), 1, 2);
        grid.add(isbnField, 2, 2);
        grid.add(new Label("What column: "), 1, 3);
        grid.add(columnField, 2, 3);
        grid.add(new Label("New column value: "), 1, 4);
        grid.add(newValueField, 2, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk
                = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel
                = new ButtonType("Go back", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter((ButtonType param) -> {
            String isbn = null;
            String title = null;
            String columnName = null;
            String newValue = null;
            if (param == buttonTypeOk) {

                isbn = isbnField.getText();
                title = nameField.getText();
                columnName = columnField.getValue();
                newValue =  newValueField.getText();
                controller.updateBook(isbn, title, columnName, newValue);

            }

            clearUpdateData();
            dialog.close();
            dialog.getDialogPane().getButtonTypes().clear();

            return null;
        });

        dialog.show();

    }

    public void frontEndRemoveBook(Controller controller) {
        Dialog<Void> dialog = new Dialog<>();
        
        

        dialog.setTitle("Remove book");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Book name: "), 1, 1);
        grid.add(nameField, 2, 1);
        grid.add(new Label("ISBN: "), 1, 2);
        grid.add(isbnField, 2, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk
                = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel
                = new ButtonType("Go back", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter((ButtonType param) -> {
            String name = null;
            String isbn = null;
          
            try{
            if (param == buttonTypeOk) {
                name = nameField.getText();
                isbn = isbnField.getText();
               controller.removeBooks(name, isbn);
              
            }
            }catch(Exception e){
                
            }
            dialog.close();
            dialog.getDialogPane().getButtonTypes().clear();
            clearRemoveBookData();
            return null;
          
        });
       

        dialog.show();

    }

    public void frontEndAddAuthorToBook(Controller controller) {
        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Add a new Author to book");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Author name "), 1, 1);
        grid.add(authorNameField, 2, 1);
        grid.add(new Label("Book name "), 1, 2);
        grid.add(bookNameField, 2, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk
                = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel
                = new ButtonType("Go back", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter((ButtonType param) -> {
            String authorName = null;
            String bookName = null;
            if (param == buttonTypeOk) {

                authorName = authorNameField.getText();
                bookName = bookNameField.getText();

                controller.addAuthorToBooks(authorName, bookName);

            }
            dialog.close();
            dialog.getDialogPane().getButtonTypes().clear();
            return null;
        });

        dialog.show();

    }

    public void frontEndLoginScreen(Controller controller) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Database Entry");
        dialog.setHeaderText("Enter your username and password");

        ButtonType buttonTypeOk
                = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel
                = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

// Create the username and password labels and fields.
        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(20, 150, 10, 10));

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");

        loginGrid.add(new Label("Username:"), 0, 0);
        loginGrid.add(usernameField, 1, 0);
        loginGrid.add(new Label("Password:"), 0, 1);
        loginGrid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(loginGrid);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }

            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {

            controller.loginScreenn(result, usernameField.getText(), passwordField.getText());
            clearLoginData();

        });

    }

    private void clearAddBookData() {
        titleField.clear();
        isbnField.clear();
        genreChoice.setValue(null);
        ratingChoice.setValue(null);
        authorNameField.clear();
        dob.setValue(null);
    }
    
    private void clearRemoveBookData(){
        nameField.clear();
        isbnField.clear();
    }

    private void clearUpdateData() {
        isbnField.setText("");
        nameField.setText("");
        columnField.setValue(null);
        newValueField.setText("");
    }

    private void clearLoginData() {
        usernameField.clear();
        passwordField.clear();
    }

    private boolean validData() {
        if (genreChoice.getValue() != null) {
            if (isbnField.getLength() == 9) {
                if (ratingChoice.getValue() != null) {
                    if (titleField.getLength() != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    
    public static void errorAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        

        alert.showAndWait();
    } 

    protected void frontEndReviews(Book tmp) {
        ArrayList<BookReviewed> reviewTmp = new ArrayList<BookReviewed>();

        try {

            reviewTmp = controller.reviews(tmp);

        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Recension");
        alert.setHeaderText("Recensioner");
        int i = reviewTmp.size();

// Create expandable Exception.
        GridPane expContent = new GridPane();

        if (i >= 1) {
            Label label = new Label("Review by: " + reviewTmp.get(0).getReviewerName() + "  Date: " + reviewTmp.get(0).getDate());
            TextArea textArea = new TextArea(reviewTmp.get(0).getReviewText());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE * 2);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);
        }

        if (i >= 2) {
            Label label1 = new Label("Review by: " + reviewTmp.get(1).getReviewerName() + "  Date: " + reviewTmp.get(1).getDate());
            TextArea textArea1 = new TextArea(reviewTmp.get(1).getReviewText());
            textArea1.setEditable(false);
            textArea1.setWrapText(true);
            expContent.add(label1, 0, 2);
            expContent.add(textArea1, 0, 3);
        }

        if (i >= 3) {
            Label label2 = new Label("Review by: " + reviewTmp.get(2).getReviewerName() + "  Date: " + reviewTmp.get(2).getDate());
            TextArea textArea2 = new TextArea(reviewTmp.get(2).getReviewText());
            expContent.add(textArea2, 0, 5);
            textArea2.setEditable(false);
            textArea2.setWrapText(true);
            expContent.add(label2, 0, 4);
        }

        if (i >= 4) {
            Label label3 = new Label("Review by: " + reviewTmp.get(3).getReviewerName() + "  Date: " + reviewTmp.get(3).getDate());

            TextArea textArea3 = new TextArea(reviewTmp.get(3).getReviewText());
            expContent.add(label3, 0, 6);
            expContent.add(textArea3, 0, 7);
            textArea3.setEditable(false);
            textArea3.setWrapText(true);
        }

        if (i >= 5) {
            Label label4 = new Label("Review by: " + reviewTmp.get(4).getReviewerName() + "  Date: " + reviewTmp.get(4).getDate());
            TextArea textArea4 = new TextArea(reviewTmp.get(4).getReviewText());
            expContent.add(label4, 0, 8);
            expContent.add(textArea4, 0, 9);
            textArea4.setEditable(false);
            textArea4.setWrapText(true);
        }

        expContent.setMaxWidth(Double.MAX_VALUE);

        alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = alert.getDialogPane().lookupButton(ButtonType.CLOSE);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

}
