import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;


public class TaskManager extends Application {
    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private FilteredList<Task> filteredTasks = new FilteredList<>(tasks);
    private Map<String, Category> categories = new HashMap<>();
    
    // Table components
    private TableView<Task> taskTable = new TableView<>();
    private TableColumn<Task, Boolean> completedCol = new TableColumn<>("Done");
    private TableColumn<Task, String> nameCol = new TableColumn<>("Task Name");
    private TableColumn<Task, String> descriptionCol = new TableColumn<>("Description");
    private TableColumn<Task, Priority> priorityCol = new TableColumn<>("Priority");
    private TableColumn<Task, LocalDate> deadlineCol = new TableColumn<>("Deadline");
    private TableColumn<Task, String> categoryCol = new TableColumn<>("Category");
    
    // Filter components
    private ComboBox<Priority> filterPriorityCombo = new ComboBox<>();
    private ComboBox<String> filterCategoryCombo = new ComboBox<>();
    private CheckBox showCompletedCheck = new CheckBox("Show Completed");
    private CheckBox showOverdueCheck = new CheckBox("Show Overdue Only");
    
    // Input components
    private ComboBox<Priority> priorityComboBox = new ComboBox<>();
    private ComboBox<String> categoryComboBox = new ComboBox<>();
    private DatePicker deadlineDatePicker = new DatePicker();
    private TextField taskNameField = new TextField();
    private TextArea descriptionArea = new TextArea();
    
    // Statistics components
    private Label totalTasksLabel = new Label("Total: 0");
    private Label completedTasksLabel = new Label("Completed: 0");
    private Label overdueTasksLabel = new Label("Overdue: 0");
    private Label highPriorityLabel = new Label("High Priority: 0");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initializeCategories();
        setupUI(primaryStage);
    }

    private void initializeCategories() {
        categories.put("Work", new Category("Work"));
        categories.put("Personal", new Category("Personal"));
        categories.put("Study", new Category("Study"));
        categories.put("Health", new Category("Health"));
        categories.put("Finance", new Category("Finance"));
        
        categoryComboBox.getItems().addAll(categories.keySet());
        filterCategoryCombo.getItems().addAll(categories.keySet());
        filterCategoryCombo.getItems().add(0, "All Categories");
        filterCategoryCombo.setValue("All Categories");
    }

    private void setupUI(Stage stage) {
        stage.setTitle("Task Genie");

        // Initialize priority combos
        priorityComboBox.getItems().addAll(Priority.values());
        priorityComboBox.setValue(Priority.MEDIUM);
        
        // Fixed: Use Priority enum only, no strings
        filterPriorityCombo.getItems().addAll(Priority.values());
        filterPriorityCombo.getItems().add(0, null); // null represents "All Priorities"
        filterPriorityCombo.setValue(null); // Default to "All Priorities"
        
        // Custom cell factory to display "All Priorities" for null
        filterPriorityCombo.setCellFactory(lv -> new ListCell<Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Priorities" : item.toString());
            }
        });
        
        filterPriorityCombo.setButtonCell(new ListCell<Priority>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Priorities" : item.toString());
            }
        });

        // Configure date picker
        deadlineDatePicker.setValue(LocalDate.now());
        deadlineDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });

        // Setup table columns
        setupTaskTable();
        
        // Setup filter controls
        setupFilters();
        
        // Setup statistics panel
        HBox statsPanel = new HBox(20, totalTasksLabel, completedTasksLabel, 
                                 overdueTasksLabel, highPriorityLabel);
        statsPanel.setPadding(new Insets(10));
        statsPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1px;");

        // Setup input form
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(10));

        inputGrid.add(new Label("Task Name:"), 0, 0);
        inputGrid.add(taskNameField, 1, 0);
        inputGrid.add(new Label("Description:"), 0, 1);
        inputGrid.add(descriptionArea, 1, 1);
        inputGrid.add(new Label("Priority:"), 0, 2);
        inputGrid.add(priorityComboBox, 1, 2);
        inputGrid.add(new Label("Category:"), 0, 3);
        inputGrid.add(categoryComboBox, 1, 3);
        inputGrid.add(new Label("Deadline:"), 0, 4);
        inputGrid.add(deadlineDatePicker, 1, 4);

        // Setup buttons - removed edit button
        Button addButton = new Button("Add Task");
        addButton.setStyle("-fx-base: #4CAF50;");
        addButton.setOnAction(e -> addTask());

        Button deleteButton = new Button("Delete Task");
        deleteButton.setStyle("-fx-base: #F44336;");
        deleteButton.setOnAction(e -> deleteTask());

        HBox buttonBox = new HBox(10, addButton, deleteButton);
        buttonBox.setPadding(new Insets(10));

        // Main layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Filter panel
        HBox filterPanel = new HBox(10, 
            new Label("Filters:"), 
            filterPriorityCombo, 
            filterCategoryCombo, 
            showCompletedCheck, 
            showOverdueCheck
        );
        filterPanel.setPadding(new Insets(10));
        filterPanel.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 1px;");

        root.getChildren().addAll(
            new Label("Task Manager"),
            statsPanel,
            inputGrid,
            buttonBox,
            filterPanel,
            taskTable
        );

        // Set up scene and stage
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Initial statistics update
        updateStatistics();
    }

    private void setupTaskTable() {
        // Make the table editable
        taskTable.setEditable(true);
        
        // Completed column with checkbox
        completedCol.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(task.isCompleted());
            
            // Update the task when the checkbox is changed
            prop.addListener((obs, oldVal, newVal) -> {
                task.setCompleted(newVal);
                updateStatistics();
                taskTable.refresh();
            });
            
            return prop;
        });
        completedCol.setCellFactory(column -> new CheckBoxTableCell<>());
        completedCol.setEditable(true);

        // Name column - Editable
        nameCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            String newValue = event.getNewValue().trim();
            if (!newValue.isEmpty()) {
                task.setName(newValue);
                taskTable.refresh();
            } else {
                // If empty name, revert to old value
                taskTable.refresh();
                showAlert("Error", "Task name cannot be empty!");
            }
        });
        nameCol.setEditable(true);

        // Description column - Editable
        descriptionCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionCol.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setDescription(event.getNewValue());
            taskTable.refresh();
        });
        descriptionCol.setEditable(true);

        // Priority column - Editable with ComboBox
        ObservableList<Priority> priorities = FXCollections.observableArrayList(Priority.values());
        priorityCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getPriority()));
        priorityCol.setCellFactory(column -> new ComboBoxTableCell<Task, Priority>(priorities) {
            @Override
            public void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority.toString());
                    if (priority == Priority.HIGH) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (priority == Priority.MEDIUM) {
                        setStyle("-fx-text-fill: orange;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
        priorityCol.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setPriority(event.getNewValue());
            updateStatistics();
            taskTable.refresh();
        });
        priorityCol.setEditable(true);

        // Deadline column - Special custom cell factory for date editing
        deadlineCol.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDeadline()));
            
        // Custom cell factory for editable LocalDate cells
        deadlineCol.setCellFactory(createDateCellFactory());
        deadlineCol.setEditable(true);

        // Category column - Editable with ComboBox
        ObservableList<String> categoryNames = FXCollections.observableArrayList(categories.keySet());
        categoryCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        categoryCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), categoryNames));
        categoryCol.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            Category category = categories.get(event.getNewValue());
            task.setCategory(category);
            taskTable.refresh();
        });
        categoryCol.setEditable(true);

        // Add columns to table
        taskTable.getColumns().addAll(completedCol, nameCol, descriptionCol, priorityCol, deadlineCol, categoryCol);
        taskTable.setItems(filteredTasks);
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Enable row selection without loading details into the form
        // No auto-loading of task details when selecting a row
    }
    
    private Callback<TableColumn<Task, LocalDate>, TableCell<Task, LocalDate>> createDateCellFactory() {
        return column -> new TableCell<Task, LocalDate>() {
            private final DatePicker datePicker = new DatePicker();
            
            {
                // Configure date picker
                datePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(date.isBefore(LocalDate.now()));
                    }
                });
                
                // Handle date selection
                datePicker.setOnAction(e -> {
                    commitEdit(datePicker.getValue());
                });
            }
            
            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    datePicker.setValue(getItem());
                    setText(null);
                    setGraphic(datePicker);
                }
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateItem(getItem(), false);
            }
            
            @Override
            public void commitEdit(LocalDate newValue) {
                super.commitEdit(newValue);
                Task task = getTableView().getItems().get(getIndex());
                task.setDeadline(newValue);
                updateStatistics();
                taskTable.refresh();
            }
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    setGraphic(null); // Reset graphic to null when not in edit mode
                    setText(date.format(DateTimeFormatter.ISO_DATE));
                    if (date.isBefore(LocalDate.now())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        };
    }

    private void setupFilters() {
        // Set up filter controls
        showCompletedCheck.setSelected(true);
        
        // Add listeners to filter controls
        filterPriorityCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        showCompletedCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        showOverdueCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        filteredTasks.setPredicate(task -> {
            // Priority filter - null means "All Priorities"
            Priority selectedPriority = filterPriorityCombo.getValue();
            if (selectedPriority != null && !task.getPriority().equals(selectedPriority)) {
                return false;
            }
            
            // Category filter
            if (!filterCategoryCombo.getValue().equals("All Categories") && 
                !task.getCategory().getName().equals(filterCategoryCombo.getValue())) {
                return false;
            }
            
            // Completed filter
            if (!showCompletedCheck.isSelected() && task.isCompleted()) {
                return false;
            }
            
            // Overdue filter
            if (showOverdueCheck.isSelected() && 
                (!task.getDeadline().isBefore(LocalDate.now()) || task.isCompleted())) {
                return false;
            }
            
            return true;
        });
        
        updateStatistics();
    }

    // Removed loadTaskForEditing method as it's no longer needed

    private void addTask() {
        String name = taskNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Task name cannot be empty!");
            return;
        }

        String description = descriptionArea.getText();
        Priority priority = priorityComboBox.getValue();
        String categoryName = categoryComboBox.getValue();
        LocalDate deadline = deadlineDatePicker.getValue();

        Category category = categories.get(categoryName);
        Task task = new RegularTask(name, description, priority, deadline, category);
        tasks.add(task);

        clearFields();
        updateStatistics();
    }

    private void deleteTask() {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tasks.remove(selected);
            updateStatistics();
        } else {
            showAlert("Error", "No task selected!");
        }
    }

    private void clearFields() {
        taskNameField.clear();
        descriptionArea.clear();
        priorityComboBox.setValue(Priority.MEDIUM);
        categoryComboBox.setValue("Work");
        deadlineDatePicker.setValue(LocalDate.now());
    }

    private void updateStatistics() {
        long total = tasks.size();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long overdue = tasks.stream()
                .filter(task -> !task.isCompleted() && task.getDeadline().isBefore(LocalDate.now()))
                .count();
        long highPriority = tasks.stream()
                .filter(task -> task.getPriority() == Priority.HIGH)
                .count();

        totalTasksLabel.setText("Total: " + total);
        completedTasksLabel.setText("Completed: " + completed + " (" + (total > 0 ? (completed * 100 / total) : 0) + "%)");
        overdueTasksLabel.setText("Overdue: " + overdue);
        highPriorityLabel.setText("High Priority: " + highPriority);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

enum Priority {
    HIGH("High"), MEDIUM("Medium"), LOW("Low");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

abstract class Task {
    private String name;
    private String description;
    private Priority priority;
    private LocalDate deadline;
    private boolean completed;
    private Category category;

    public Task(String name, String description, Priority priority, LocalDate deadline, Category category) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.category = category;
        this.completed = false;
    }

    public abstract String getTaskType();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}

class RegularTask extends Task {
    public RegularTask(String name, String description, Priority priority, LocalDate deadline, Category category) {
        super(name, description, priority, deadline, category);
    }

    @Override
    public String getTaskType() {
        return "Regular Task";
    }
}

class Category {
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
