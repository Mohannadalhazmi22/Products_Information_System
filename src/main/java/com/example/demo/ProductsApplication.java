package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


public class ProductsApplication extends Application {
    static final String DB_URL = "jdbc:mysql://localhost:3306/productsdb_alhazmi_nourwali";

    static final String USER = "root";
    static final String PASS = "root"; // write your own password

    static Connection connection = null;
    static PreparedStatement pStatement = null;
    static ResultSet resultSet = null;

    static Scene rootScene; //The add scene
    static VBox name; //static so we can add the page name to it in each page
    static TableView<Products> productsTable = null;

    //Launch the program
    public static void main(String[] args) {
        launch();
    }


    public BorderPane menuBar(Stage stage){
        //Setting up our names:
        Label Mohannad = new Label("Student #1 : Mohannad Al-Hazmi, 442006217, s442006217@st.uqu.edu.sa");
        Label Omir = new Label("Student #2 : Omir Abbas, 442011398, s442011398@st.uqu.edu.sa");
        Mohannad.setPadding(new Insets(3,0,0,250));
        Mohannad.setFont(new Font(15));
        Omir.setPadding(new Insets(3,0,20,250));
        Omir.setFont(new Font(15));
        //create a VBox to put the menuBar and the names in it
        name = new VBox();
        //create a border pane
        BorderPane pane = new BorderPane();
        //creating the menu and put some items in it
        Menu Products = new Menu("Products");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(Products);
        Menu Product = new Menu("Product");
        MenuItem Exit = new MenuItem("Exit");
        //if the user clicked exit:
        Exit.setOnAction(e -> System.exit(0));
        Products.getItems().addAll(Product,Exit);
        MenuItem Add = new MenuItem("Add");
        //if the user clicked on ADD
        Add.setOnAction(e ->{
            stage.setScene(rootScene);
        });

        MenuItem Search = new MenuItem("Search");
        //if the user clicked on SEARCH
        Search.setOnAction(e ->{
            Scene searchScene = new Scene(Search(stage),1000,800);
            stage.setScene(searchScene);
        });

        MenuItem Delete = new MenuItem("Delete");
        //if the user clicked on DELETE
        Delete.setOnAction(e-> {
            Scene deleteScene = new Scene(delete(stage),1000,800);
            stage.setScene(deleteScene);
        });
        //put the items in the Product menu
        Product.getItems().addAll(Add,Search,Delete);
        //put the menuBar and the names in the VBox
        name.getChildren().addAll(menuBar, Mohannad,Omir);
        //put the VBox in the top of the border pane
        pane.setTop(name);

        return pane;
    }



    @Override
    public void start(Stage stage) {
        //Setting up the border pane
        BorderPane rootPane = menuBar(stage);
        rootPane.setPadding(new Insets(0,0,300,0));
        //Setting up the Page Name
        Label pageName = headerLabels("Add Page");
        name.getChildren().add(pageName);
        VBox.setMargin(pageName,new Insets(20,0,20,350));
        //Setting up the grid pane
        GridPane addPane = new GridPane();
        addPane.setPadding(new Insets(15,100,15,300));
        addPane.setHgap(5);
        addPane.setVgap(20);
        //Add items into the grid pane
        //Add Type
        addPane.add(new Label("Type: "),0,0);
        ChoiceBox type = new ChoiceBox();
        type.getItems().addAll("TV","Fridge","Laptop","PC","Air Conditioner","Vacuum Cleaner");
        addPane.add(type,1,0);
        //Add Model
        addPane.add(new Label("Model: "),0,1);
        TextField model = new TextField();
        addPane.add(model,1,1);
        //Add Price
        addPane.add(new Label("Price: "),0,2);
        TextField price = new TextField();
        addPane.add(price,1,2);
        //Add Count
        addPane.add(new Label("Count: "),0,3);
        Slider count = new Slider(0.0,10,0.0);
        count.setShowTickLabels(true);
        count.setMajorTickUnit(1);
        count.setBlockIncrement(1);
        addPane.add(count,1,3);
        //Add Date
        addPane.add(new Label("Delivery Date: "),0,4);
        DatePicker date = new DatePicker();
        addPane.add(date,1,4);
        //Add SAVE button
        Button save = new Button("Save");
        addPane.add(save,1,5);
        GridPane.setHalignment(save, HPos.RIGHT);
        save.setFont(new Font(15));

        save.setOnAction(e ->{
            //if the user clicked on SAVE button

            boolean isAdded =false;
            //read the Type from the Choice box
            String getType= (String)type.getValue();
            if(getType==null){
                //check if the user didn't choose any Type and display error
                Label emptyType = new Label("Type field can't be empty");
                errorLabels(emptyType);
                rootPane.setBottom(emptyType);
            }else {
                //read the Model from the text field
                String getModel = model.getText();
                if(getModel.equals("")){
                    //check if the Model field is empty and display error
                    Label emptyModel = new Label("Model field can't be empty");
                    errorLabels(emptyModel);
                    rootPane.setBottom(emptyModel);
                }else {
                    //read the price from the text field as a string (later will be casted into double and checks the error)
                    String getPrice = price.getText();
                    //read the count from the slider
                    int getCount = (int) count.getValue();
                    if(getCount==0){
                        //checks if it's still 0 (the default value) and display error if so
                        Label emptyCount = new Label("Count field can't be 0 ");
                        errorLabels(emptyCount);
                        rootPane.setBottom(emptyCount);
                    }else {
                        //read the date from the DatePicker
                        LocalDate localDate = date.getValue();
                        if(localDate==null){
                            //check if the date field is empty and display error
                            Label emptyDate = new Label("Date field can't be empty ");
                            errorLabels(emptyDate);
                            rootPane.setBottom(emptyDate);
                        }else{
                        String strDate = localDate.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
                        Date getDate = Date.valueOf(strDate);
                        //pass all the information to the addInDB method
                        isAdded = addInDB(getType, getModel, getPrice, getCount, getDate,rootPane);
                        }
                        if(isAdded){
                            //clear the fields if added successfully.
                            type.setValue(null);
                            model.clear();
                            price.clear();
                            count.setValue(0);
                            date.setValue(null);
                        }

                    }
                }
            }
        });
        //Put the grid pane in the border pane to display everything
        rootPane.setCenter(addPane);
        ///////////////////////////////////////////////////////
        //setting up the stage and show it
        rootScene = new Scene(rootPane, 1000,800);
        stage.setTitle("Product Information System");
        stage.setScene(rootScene);
        stage.show();
    }



    private boolean addInDB(String type, String model, String unHandledPrice, int count, Date date, BorderPane rootPane) {
        openConnection();
        try {
            double price = Double.parseDouble(unHandledPrice);
            //Preparing the statement...
            pStatement = connection.prepareStatement("INSERT INTO ProductsTBL_Mohannad_Omir(type,model,price,count,DelivaryDate) values (?,?,?,?,?)");
            int columnNumber = 1;
            //Getting the Type from the user and pass it to the right parameter
            pStatement.setString(columnNumber,type);
            columnNumber++;
            //Getting the Model from the user and pass it to the right parameter
            pStatement.setString(columnNumber,model);
            columnNumber++;
            if (price<=0){
                //if the price is 0 or less display an error
                Label LABELPriceError = new Label("Wrong price, Only positive numbers.");
                errorLabels(LABELPriceError);
                rootPane.setBottom(LABELPriceError);
                closeConnection();
                return false;
            }
            //Getting the Price from the user and pass it to the right parameter
            pStatement.setDouble(columnNumber,price);
            columnNumber++;
            //Getting the Number of products from the user and pass it to the right parameter
            pStatement.setInt(columnNumber,count);
            columnNumber++;
            //Getting the Date from the user and pass it to the right parameter
            pStatement.setDate(columnNumber,date);
            //execute the insertion
            int k = pStatement.executeUpdate();
            if(k==1){
                //checks if it is done or not
                Label LABELAdditionSuccess = new Label("adding the product has been done successfully");
                successLabels(LABELAdditionSuccess);
                rootPane.setBottom(LABELAdditionSuccess);

            }

        }
        catch (Exception InputMismatchException){
            //if the user's entered a string in price field or left it empty
            Label LABELWrongInputType = new Label("Price field can't be empty and you can't enter a string");
            errorLabels(LABELWrongInputType);
            rootPane.setBottom(LABELWrongInputType);
            closeConnection();
            return false;
        }
        finally {
            closeConnection();
        }
        return true;
    }

    public BorderPane Search(Stage stage){
        //set up the table
        productsTable = new TableView<>();
        initializeTable();

        //set up the border pane
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(0,0,15,0));
        borderPane.setTop(menuBar(stage));
        //Put the page name
        Label pageName = headerLabels("Search Page");
        VBox.setMargin(pageName,new Insets(20,0,20,350));
        name.getChildren().add(pageName);
        //set up the grid pane
        GridPane searchPane = new GridPane();
        searchPane.setPadding(new Insets(15,100,15,100));
        searchPane.setVgap(10);
        searchPane.setHgap(5);
        //put the elements in the grid pane
        Label label = new Label("Enter The Type Or Model: ");
        label.setFont(new Font(15));
        TextField textField = new TextField();
        textField.setPrefColumnCount(30);

        searchPane.add(label,0,0);
        searchPane.add(textField,0,1);
        searchPane.add(productsTable,0,2);
        //put the gird pane in the border pane
        borderPane.setCenter(searchPane);
        //Add the buttons to the grid pane
        Button searchButton = new Button("Search");
        searchButton.setFont(new Font(15));
        Button refreshButton = new Button("Refresh");
        refreshButton.setFont(new Font(15));
        searchPane.add(searchButton,2,1);
        searchPane.add(refreshButton,3,1);

        searchButton.setOnAction(e -> {
            //If the user clicked on the SEARCH button
            productsTable.getItems().clear();
            borderPane.setBottom(new Label(""));
            String modelOrType = textField.getText();
            searchInDB(modelOrType,borderPane);
        });

        refreshButton.setOnAction(e -> {
            //If the user clicked on the REFRESH button
            //Basically both buttons have the same functionality
            productsTable.getItems().clear();
            borderPane.setBottom(new Label(""));
            String modelOrType = textField.getText();
            searchInDB(modelOrType,borderPane);
        });


        return borderPane;
    }

    public void searchInDB(String modelOrType , BorderPane pane){

        openConnection();
        try {

            pStatement = connection.prepareStatement("SELECT * FROM ProductsTBL_Mohannad_Omir WHERE model LIKE ? OR type LIKE ?");
            pStatement.setString(1,"%"+modelOrType+"%");
            pStatement.setString(2,"%"+modelOrType+"%");
            resultSet = pStatement.executeQuery(); //execute the search
            if(!resultSet.isBeforeFirst()){
                Label NoResultFound = new Label("No records available for this search criteria !");
                errorLabels(NoResultFound);
                pane.setBottom(NoResultFound);
                //if there is no result from the search
            }
            else {
                //Put the ResultSet into the ObservableList
                ObservableList<Products> productsList = FXCollections.observableArrayList();
                while (resultSet.next()){
                    Products product =  new Products(resultSet.getInt(1),resultSet.getString(2),resultSet.getString(3),resultSet.getDouble(4),
                            resultSet.getInt(5),resultSet.getDate(6));
                    productsList.add(product);
                }
                //put the items in the table
                productsTable.setItems(productsList);

            }
        }
        catch (Exception e){
            e.printStackTrace();   // here there is no error except maybe in the pStatment
        }
        finally {
            closeConnection();
        }
    }
    public void initializeTable(){
        //This is a method to set up the table by inserting the columns and set the properties
        TableColumn<Products,Integer> fID = new TableColumn<>("ID");
        fID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        fID.setSortable(true);
        fID.setResizable(true);
        fID.setPrefWidth(70);

        TableColumn<Products,String> fType = new TableColumn<>("type");
        fType.setCellValueFactory(new PropertyValueFactory<>("type"));
        fType.setSortable(true);
        fType.setResizable(true);
        fType.setPrefWidth(100);

        TableColumn<Products,String> fModel = new TableColumn<>("model");
        fModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        fModel.setSortable(true);
        fModel.setResizable(true);
        fModel.setPrefWidth(130);

        TableColumn<Products,Double> fPrice = new TableColumn<>("price");
        fPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        fPrice.setSortable(true);
        fPrice.setResizable(true);
        fPrice.setPrefWidth(130);

        TableColumn<Products,Integer> fCount = new TableColumn<>("count");
        fCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        fCount.setSortable(true);
        fCount.setResizable(true);
        fCount.setPrefWidth(70);

        TableColumn<Products, java.util.Date> fDate = new TableColumn<>("DeliveryDate");
        fDate.setCellValueFactory(new PropertyValueFactory<>("DeliveryDate"));
        fDate.setSortable(true);
        fDate.setResizable(true);
        fDate.setPrefWidth(100);
        productsTable.getColumns().addAll(fID,fType,fModel,fPrice,fCount,fDate);
        productsTable.setPrefSize(600,600);
    }
    public BorderPane delete(Stage stage){
        //set up the border Pane
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(0,0,300,0));
        borderPane.setTop(menuBar(stage));
        //Put the Page name
        Label pageName = headerLabels("Delete Page");
        name.getChildren().add(pageName);
        VBox.setMargin(pageName,new Insets(20,0,20,350));
        //set up the grid pane
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(50,100,15,300));
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        //Put the elements
        Label label = new Label("Enter the ID of the product for deletion: ");
        label.setFont(new Font(15));
        gridPane.add(label,0,0);
        TextField id = new TextField();
        gridPane.add(id,0,1);
        Button delete = new Button("Delete");
        delete.setFont(new Font(15));
        gridPane.add(delete,1,1);
        //put the grid pane in the border pane
        borderPane.setCenter(gridPane);
        delete.setOnAction(e ->{
            //if the user clicked on delete
            String getId = id.getText();
            boolean isDeleted = deleteInDB(getId,borderPane);
            if(isDeleted) {
                id.setText("");
            }
        });
        return borderPane;
    }

    public boolean deleteInDB(String unHandledID,BorderPane deletePane){
        openConnection();
        try {
            int productId = Integer.parseInt(unHandledID); //Convert the ID into Integer and check if there is an error
            if(productId<=0){
                //Give an error if the ID is 0 or less
                Label wrongIDError = new Label("Wrong ID number, Only positive numbers are allowed.");
                errorLabels(wrongIDError);
                deletePane.setBottom(wrongIDError);
            }else {
                pStatement = connection.prepareStatement("SELECT * FROM ProductsTBL_Mohannad_Omir WHERE ID = ?");
                pStatement.setInt(1, productId);
                resultSet = pStatement.executeQuery(); //execute SELECT to see if there is an element with this ID
                if (resultSet.isBeforeFirst()) {
                    //if there is an element with this ID:
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setContentText("Are you sure you want to delete this product?");
                    Optional<ButtonType> yesOrNo = confirm.showAndWait(); //Wait for the user to confirm or cancel
                    if (yesOrNo.get() == ButtonType.OK) {
                        //If yes then delete
                        pStatement = connection.prepareStatement("DELETE FROM ProductsTBL_Mohannad_Omir WHERE ID =?");
                        pStatement.setInt(1, productId);
                        pStatement.executeUpdate(); //Execute Deletion
                        Label deletionSuccess = new Label("Success, Record deleted.");
                        successLabels(deletionSuccess);
                        deletePane.setBottom(deletionSuccess);
                        return true;
                    } else {
                        //if the user cancelled:
                        Label deletionCancelled = new Label("Deletion is cancelled.");
                        errorLabels(deletionCancelled);
                        deletePane.setBottom(deletionCancelled);
                        closeConnection();
                    }

                } else {
                    //if there is no element with the entered ID
                    Label recordNotFound = new Label("Record is not found!!");
                    errorLabels(recordNotFound);
                    deletePane.setBottom(recordNotFound);
                    closeConnection();
                }
            }

        }
        catch (Exception InputMismatchException){
            //if the user entered a string instead of a number in ID
            Label IDError = new Label("You can't enter a string in id, just numbers, and you can't leave this field empty");
            errorLabels(IDError);
            deletePane.setBottom(IDError);
        }
        finally {
            closeConnection();
        }
        return false;
    }
    public void errorLabels(Label error){
        //Setting up a style for ERROR messages
        error.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        error.setTextFill(Color.RED);
        error.setBackground(Background.fill(Color.ANTIQUEWHITE));
        BorderPane.setAlignment(error,Pos.TOP_CENTER);
    }
    public void successLabels(Label success){
        //Setting up a style for SUCCESS messages
        success.setFont((Font.font("Verdana", FontWeight.BOLD, 20)));
        success.setTextFill(Color.GREEN);
        success.setBackground(Background.fill(Color.ANTIQUEWHITE));
        BorderPane.setAlignment(success,Pos.TOP_CENTER);
    }
    public Label headerLabels(String page){
        //Setting up a style for the PAGE NAME labels
        Label header = new Label(page);
        header.setFont(Font.font("Verdana", FontWeight.BOLD, 35));
        header.setTextFill(Color.DARKCYAN);
        BorderPane.setAlignment(header,Pos.CENTER);
        return header;
    }


    public static void openConnection() {
        //a method to open the connection
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void closeConnection(){
        // a method to close all connections
        try {
            if(connection!=null)
                connection.close(); //close connection
        }   catch (Exception e){e.printStackTrace();}

        try {
            if(pStatement!=null)
                pStatement.close(); //close prepared statement
        }catch (Exception e){e.printStackTrace();}

        try {
            if(resultSet!=null)
                resultSet.close(); //close result set
        }catch (Exception e){e.printStackTrace();}

    }


}