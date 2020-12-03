package main;

import com.mongodb.MongoException;
import view.BooksPane;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.MongoDb;

/**
 * Application start up.
 *
 * @author anderslm@kth.se
 */
public class Databaselab extends Application {

    @Override
    public void start(Stage primaryStage) {

        MongoDb booksDb = new MongoDb(); // model

        BooksPane root = new BooksPane(booksDb);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Books Database Client");

        // Event handler when pressing X
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent paramT) {

              if (booksDb.getConnection() != null) {
                    try {
                        booksDb.disconnect();

                    } catch (MongoException ex) {
                        ex.printStackTrace();
                    }
              }

            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
