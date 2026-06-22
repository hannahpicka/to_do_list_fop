package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.*;
import javafx.scene.input.MouseEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;

public class Controller {
    @FXML private VBox ListContainer;
    @FXML private TextField addTask;
    @FXML private Label lblCounter;
    @FXML private ChoiceBox<String> choice;
    @FXML private Label sort;

    private ObservableList<String> dataList = FXCollections.observableArrayList();
    private List<HBox> hboxList = new ArrayList<>();
    
    private int totalTasks = 0;
    private int completedTasks = 0;
	private ChoiceBox<String> datePicker;
	private ChoiceBox<String> priorityChoice;

    @FXML
    public void initialize() {
        choice.setItems(FXCollections.observableArrayList(
            "Due Date Ascending", "Due Date Descending",
            "Priority High to Low", "Priority Low to High"
        ));

        choice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String sortType = choice.getSelectionModel().getSelectedItem();
                sort.setText(sortType);  // Update the label to show the selected sorting option

                switch (sortType) {
                    case "Due Date Ascending":
                        hboxList.sort(Comparator.comparing(Controller.this::getDueDate));
                        break;
                    case "Due Date Descending":
                        hboxList.sort(Comparator.comparing(Controller.this::getDueDate).reversed());
                        break;
                    case "Priority High to Low":
                        hboxList.sort(Comparator.comparing(Controller.this::getPriority).reversed());
                        break;
                    case "Priority Low to High":
                        hboxList.sort(Comparator.comparing(Controller.this::getPriority));
                        break;
                }

                refreshListContainer();
            }
        });

        ListContainer.setSpacing(10);
        ListContainer.setPadding(new Insets(10, 10, 10, 10));
    }



    @FXML
    public void handleAddButtonClick(MouseEvent event) {
        String task = addTask.getText();
        if (!task.isEmpty()) {
            addTask.clear();
            dataList.add(task);
            totalTasks++;
            HBox hbox = createHBox(task);
            ListContainer.getChildren().add(hbox);
            hboxList.add(hbox);
            updateTaskCount();
        }
    }

    private HBox createHBox(String task) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);
        hbox.setStyle("-fx-background-color: #3d3d3d; -fx-background-radius: 10px;");
        hbox.setPadding(new Insets(10, 10, 10, 10));
        
        Label taskLabel = new Label(task);
        taskLabel.setStyle("-fx-text-fill: white;");
        taskLabel.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, FontPosture.REGULAR, 14));
        
        DatePicker datePicker = new DatePicker();       
        datePicker.setPromptText("Due Date");
        datePicker.setOnAction(e -> datePicker.setDisable(true));
        
        ChoiceBox<String> priorityChoice = new ChoiceBox<>();
        priorityChoice.getItems().add("Select Priority");
        priorityChoice.setItems(FXCollections.observableArrayList("Low", "Medium", "High"));
        priorityChoice.setValue("Select Priority");
        priorityChoice.setStyle(
        		"-fx-border-color: #aaaaaa; " +
        		"-fx-background-radius: 50px; " +
        		"-fx-border-radius: 50px;"
        		);
        priorityChoice.setOnAction(e -> priorityChoice.setDisable(true));
        
        hbox.setUserData(priorityChoice);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        editButton.setStyle(
                "-fx-background-color: #80ed99; " +
                "-fx-background-radius: 50px; " +
                "-fx-text-fill: #213a57;"
            );
        editButton.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, FontPosture.REGULAR, 10));
        
        deleteButton.setStyle(
                "-fx-background-color: #ac1d1b; " +
                "-fx-background-radius: 50px; " +
                "-fx-text-fill: white;"
            );
        deleteButton.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, FontPosture.REGULAR, 10));

        editButton.setOnAction(e -> editTask(hbox, task));
        deleteButton.setOnAction(e -> deleteTask(hbox));
        
        Image incompleteImage = new Image("file:/C:/Users/hp/Desktop/icons8-circle-32.png");
        Image completeImage = new Image("file:/C:/Users/hp/Desktop/icons8-checkmark-32.png");

        ImageView statusIcon = new ImageView(incompleteImage);
        statusIcon.setFitWidth(20);  // Set appropriate size
        statusIcon.setFitHeight(20);

        statusIcon.setOnMouseClicked(e -> {
            if (statusIcon.getImage().equals(incompleteImage)) {
                statusIcon.setImage(completeImage);
                hbox.setStyle("-fx-background-color: #80ed99; -fx-background-radius: 10px;");
                taskLabel.setStyle("-fx-text-fill: #213a57;");
                markTaskAsCompleted(hbox);
                editButton.setVisible(false);
                priorityChoice.setVisible(false);
                datePicker.setVisible(false);
            }
        });

        editButton.setCursor(Cursor.HAND);
        deleteButton.setCursor(Cursor.HAND);
        statusIcon.setCursor(Cursor.HAND);
        
        hbox.getChildren().addAll(statusIcon, taskLabel, spacer, datePicker, priorityChoice, editButton, deleteButton);
        return hbox;
    }

    private void editTask(HBox hbox, String oldTask) {
        int index = hboxList.indexOf(hbox);
        if (index >= 0) {
            TextField editField = new TextField(oldTask);
            DatePicker editDatePicker = new DatePicker();

            ChoiceBox<String> priorityChoice = (ChoiceBox<String>) hbox.getUserData();
            ChoiceBox<String> editPriorityChoice = new ChoiceBox<>(FXCollections.observableArrayList("Low", "Medium", "High"));
            editPriorityChoice.setValue(priorityChoice.getValue());

            DatePicker existingDatePicker = (DatePicker) hbox.getChildren().get(3);
            editDatePicker.setValue(existingDatePicker.getValue());

            VBox editLayout = new VBox();
            editLayout.setSpacing(10);
            editLayout.setPadding(new Insets(10));
            
            Button saveButton = new Button("Save");
            saveButton.setOnAction(e -> {
                String newText = editField.getText();
                if (!newText.isEmpty()) {
                    dataList.set(index, newText);
                    ((Label) hbox.getChildren().get(1)).setText(newText);

                    priorityChoice.setValue(editPriorityChoice.getValue());
                    existingDatePicker.setValue(editDatePicker.getValue());

                    ((Stage) saveButton.getScene().getWindow()).close();
                }
            });

            editLayout.getChildren().addAll(
                new Label("Edit Task:"), editField,
                new Label("Edit Due Date:"), editDatePicker,
                new Label("Edit Priority:"), editPriorityChoice,
                saveButton
            );

            Stage editStage = new Stage();
            editStage.setTitle("Edit Task");
            editStage.setScene(new Scene(editLayout, 300, 300));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.show();
        }
    }

    private void deleteTask(HBox hbox) {
        int index = hboxList.indexOf(hbox);
        if (index >= 0) {
        	if (hbox.getStyle().contains("#80ed99")) {
        		completedTasks--;
        	}
        	
            dataList.remove(index);
            totalTasks--;
            ListContainer.getChildren().remove(hbox);
            hboxList.remove(index);
            updateTaskCount();
        }
    }

    private void updateTaskCount() {
        lblCounter.setText(completedTasks + "/" + totalTasks);
    }

    private void refreshListContainer() {
        ListContainer.getChildren().clear();
        for (HBox hbox : hboxList) {
            ListContainer.getChildren().add(hbox);
        }
    }
    
    private void markTaskAsCompleted (HBox hbox) {
    	completedTasks++;
    	updateTaskCount();
    }
    
    private LocalDate getDueDate(HBox hbox) {
        DatePicker datePicker = (DatePicker) hbox.getChildren().get(3);
        return datePicker.getValue() != null ? datePicker.getValue() : LocalDate.MAX;
    }

    private int getPriority(HBox hbox) {
        ChoiceBox<String> priorityChoice = (ChoiceBox<String>) hbox.getUserData();
        switch (priorityChoice.getValue()) {
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 0;
        }
    }

}
