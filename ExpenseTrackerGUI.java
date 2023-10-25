package ExpenseTrackerGUI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ExpenseTrackerGUI extends Application {
    private final ArrayList<Expense> expenses = new ArrayList<>();
    public static void main(String[] args) {
        launch(args);
    }
  
    @Override
    public void start(Stage stage) {
        stage.setTitle("Expense Tracker");
      
        // Input Fields
        TextField descriptionField = new TextField();
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Groceries", "Transportation", "Entertainment", "Other");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");

        // Buttons
        Button addButton = new Button("Add Expense");
        Button saveButton = new Button("Save Expenses");
        Button loadButton = new Button("Load Expenses");
      
        // Expense List
        TextArea expenseListArea = new TextArea();
        expenseListArea.setEditable(false);
        expenseListArea.setPrefHeight(200);
      
        // Filters
        ComboBox<String> categoryFilterComboBox = new ComboBox<>();
        categoryFilterComboBox.getItems().addAll("All Categories", "Groceries", "Transportation", "Entertainment", "Other");
        categoryFilterComboBox.setValue("All Categories");
        Label totalCategoryExpensesLabel = new Label("Total Expenses: $0.00");
      
        // Layout
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.addRow(0, new Label("Description:"), descriptionField);
        inputGrid.addRow(1, new Label("Amount ($):"), amountField);
        inputGrid.addRow(2, new Label("Category:"), categoryComboBox);
        inputGrid.addRow(3, new Label("Date:"), datePicker);
        VBox buttonBox = new VBox(10, addButton, saveButton, loadButton);
        VBox filterBox = new VBox(10, new Label("Category Filter:"), categoryFilterComboBox, totalCategoryExpensesLabel);
        HBox topBox = new HBox(20, inputGrid, buttonBox);
        HBox.setHgrow(inputGrid, Priority.ALWAYS);
        VBox root = new VBox(20, topBox, expenseListArea, filterBox);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 600, 400);
      
        // Event Handlers
        addButton.setOnAction(event -> {
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryComboBox.getValue();
            LocalDate date = datePicker.getValue();
            expenses.add(new Expense(description, amount, category, date));
            updateExpenseList(expenseListArea);
            clearInputFields(descriptionField, amountField, datePicker);
        });
        categoryFilterComboBox.setOnAction(event -> {
            String selectedCategory = categoryFilterComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            double totalExpenses = calculateTotalExpenses(selectedCategory, selectedDate);
            totalCategoryExpensesLabel.setText("Total Expenses: $" + String.format("%.2f", totalExpenses));
        });
      
        // Save Button
        saveButton.setOnAction(event -> saveExpensesToFile());
        // Load Button
        loadButton.setOnAction(event -> {
            expenses.clear(); // Clear the existing expenses
            loadExpensesFromFile(); // Load expenses from the file
            updateExpenseList(expenseListArea); // Update the displayed expense list
        });
        stage.setScene(scene);
        stage.show();
    }
  
    private void clearInputFields(TextField descriptionField, TextField amountField, DatePicker datePicker) {
        descriptionField.clear();
        amountField.clear();
        datePicker.getEditor().clear();
    }
    private void saveExpensesToFile() {
        try (PrintWriter writer = new PrintWriter("expenses.txt")) {
            expenses.forEach(expense -> {
                writer.println(expense.toFileString());
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace(); // Log the exception
        }
    }
    private void loadExpensesFromFile() {
        File file = new File("expenses.txt");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                expenses.clear(); // Clear the existing expenses
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    Expense expense = Expense.fromString(line);
                    expenses.add(expense);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace(); // Log the exception
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace(); // Log the exception
            }
        }
    }
    private double calculateTotalExpenses(String category, LocalDate selectedDate) {
        double total = 0.0;
        total = expenses.stream()
                .filter((expense) -> (expense.category.equals(category) || category.equals("All Categories"))
                        && (selectedDate == null || expense.date.isEqual(selectedDate)))
                .map((expense) -> expense.amount)
                .reduce(total, (accumulator, item) -> accumulator + item);
        return total;
    }
    private void updateExpenseList(TextArea expenseListArea) {
        expenseListArea.clear();
        expenses.forEach(expense -> {
            expenseListArea.appendText(expense + "\n");
        });
    }
    static class Expense {
        String description;
        double amount;
        String category;
        LocalDate date;
        public Expense(String description, double amount, String category, LocalDate date) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }
        
        @Override
        public String toString() {
            return "Description: " + description + ", Amount: $" + amount + ", Category: " + category + ", Date: " + date;
        }
        
        public String toFileString() {
            return description + "|" + amount + "|" + category + "|" + date;
        }
        
        public static Expense fromString(String line) {
            String[] parts = line.split("\\|");
            String description = parts[0];
            double amount = Double.parseDouble(parts[1]);
            String category = parts[2];
            LocalDate date = LocalDate.parse(parts[3]);
            return new Expense(description, amount, category, date);
        }
    }
}
