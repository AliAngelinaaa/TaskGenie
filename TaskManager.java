import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TaskManager extends Application {
    private ObservableList<String> tasks;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Task Manager");
        
        tasks = FXCollections.observableArrayList();
        ListView<String> taskListView = new ListView<>(tasks);
        
        TextField taskInput = new TextField();
        taskInput.setPromptText("Enter a task");
        
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));
        priorityBox.setPromptText("Select Priority");
        
        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            String taskText = taskInput.getText();
            String priority = priorityBox.getValue();
            if (!taskText.isEmpty() && priority != null) {
                tasks.add(taskText + " [" + priority + "]");
                taskInput.clear();
                priorityBox.getSelectionModel().clearSelection();
            }
        });
        
        Button deleteButton = new Button("Delete Task");
        deleteButton.setOnAction(e -> {
            String selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                tasks.remove(selectedTask);
            }
        });
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(taskInput, priorityBox, addButton, taskListView, deleteButton);
        
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
