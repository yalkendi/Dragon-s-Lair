import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
//import javafx.scene.control.Cell;
import javafx.scene.control.Cell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    private boolean unsaved = false;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableView<Order> customerOrderTable;
    @FXML private TableView<Title> titleTable;
    @FXML private TableView<FlaggedTable> flaggedTable;                                         //Jack
    @FXML private TableView<RequestTable> requestsTable;
    @FXML private TableColumn<Customer, String> customerLastNameColumn;
    @FXML private TableColumn<Customer, String> customerFirstNameColumn;
    @FXML private TableColumn<Customer, String> customerPhoneColumn;
    @FXML private TableColumn<Customer, String> customerEmailColumn;

    @FXML private TableColumn<Title, Boolean> titleFlaggedColumn;
    @FXML private TableColumn<Title, String> titleTitleColumn;
    @FXML private TableColumn<Title, String> titlePriceColumn;
    @FXML private TableColumn<Title, String> titleNotesColumn;

    @FXML private TableColumn<Order, String> customerOrderReqItemsColumn;
    @FXML private TableColumn<Order, String> customerOrderQuantityColumn;
    @FXML private TableColumn<Order, String> customerOrderIssueColumn;

    @FXML private TableColumn<FlaggedTable, String> flaggedTitleColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedIssueColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedPriceColumn;             //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedQuantityColumn;          //Jack
    @FXML private TableColumn<FlaggedTable, String> flaggedNumRequestsColumn;

    @FXML private TableColumn<RequestTable, String> requestLastNameColumn;
    @FXML private TableColumn<RequestTable, String> requestFirstNameColumn;
    @FXML private TableColumn<RequestTable, Integer> requestQuantityColumn;

    @FXML private Text customerFirstNameText;
    @FXML private Text customerLastNameText;
    @FXML private Text customerPhoneText;
    @FXML private Text customerEmailText;

    @FXML private Text titleTitleText;
    @FXML private Text titlePriceText;
    @FXML private Text titleNotesText;
    @FXML private Text titleDateFlagged;
    @FXML private Text titleDateFlaggedNoticeText;
    @FXML private Text titleNumberRequestsText;

    //for the summary info in "new week pulls" tab in "reports" tab
    @FXML private Text FlaggedTitlesTotalText;
    @FXML private Text FlaggedTitlesTotalCustomersText;
    @FXML private Text FlaggedIssueNumbersText;
    @FXML private Text FlaggedNoRequestsText;

    //for the summary info on a particular flagged title, when clicked
    @FXML private Text RequestTitleText;
    @FXML private Text RequestQuantityText;
    @FXML private Text RequestNumCustomersText;

    private static Connection conn = null;

    /**
     * Initiializes the state of the application. Creates a connection to the database,
     * loads all Customer, Title, and Order data, populates all tables, and creates
     * listeners.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createConnection();

        //Populate columns for Customer Table
        customerLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        customerFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerTable.getItems().setAll(this.getCustomers());

        //Populate columns for Orders Table
        customerOrderReqItemsColumn.setCellValueFactory(new PropertyValueFactory<>("TitleName"));
        customerOrderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        customerOrderIssueColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getIssue() > 0) {
                return new SimpleStringProperty(Integer.toString(cell.getValue().getIssue()));
            } else {
                return new SimpleStringProperty("");
            }
        });

        //Populate columns for Title Table
        titleFlaggedColumn.setCellValueFactory(c -> c.getValue().flaggedProperty());
        titleFlaggedColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        titleTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titlePriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceDollars"));
        titlePriceColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getPrice() > 0) {
                return new SimpleStringProperty(cell.getValue().getPriceDollars());
            } else {
                return new SimpleStringProperty("");
            }
        });
        titleNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        titleTable.getItems().setAll(this.getTitles());

        //Populate columns for flagged titles table in New Week Pulls Tab
        flaggedTitleColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedTitleName"));
        flaggedIssueColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getFlaggedIssueNumber() > 0) {
                return new SimpleStringProperty(Integer.toString(cell.getValue().getFlaggedIssueNumber()));
            } else {
                return new SimpleStringProperty("");
            }
        });
        flaggedPriceColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getFlaggedPriceCents() > 0) {
                return new SimpleStringProperty(cell.getValue().getFlaggedPriceDollars());
            } else {
                return new SimpleStringProperty("");
            }
        });
        flaggedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedQuantity"));
        flaggedNumRequestsColumn.setCellValueFactory(new PropertyValueFactory<>("flaggedNumRequests"));

        //for requests table
        requestLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestLastName"));
        requestFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("RequestFirstName"));
        requestQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("RequestQuantity"));

        //Load the data for the Reports tab
        this.loadReportsTab();

        //Add Listener for selected Customer
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                customerFirstNameText.setText(newSelection.getFirstName());
                customerLastNameText.setText(newSelection.getLastName());
                customerPhoneText.setText(newSelection.getPhone());
                customerEmailText.setText(newSelection.getEmail());
                updateOrdersTable(newSelection);
            }
        });

        //Add Listener for Titles table
        titleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleTitleText.setText(newSelection.getTitle());
                if (newSelection.getPrice() > 0) {
                    titlePriceText.setText(newSelection.getPriceDollars());
                } else {
                    titlePriceText.setText("");
                }
                titleNotesText.setText(newSelection.getNotes());
                String numberRequests = String.format("This Title Currently has %s Customer Requests", getNumberRequests(newSelection.getId()));
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                if (newSelection.getDateFlagged() != null) {
                    titleDateFlagged.setText(newSelection.getDateFlagged().toString());
                    if (newSelection.getDateFlagged().isBefore(sixMonthsAgo)) {
                        titleDateFlaggedNoticeText.setVisible(true);
                    }
                    else {
                        titleDateFlaggedNoticeText.setVisible(false);
                    }
                }
                else {
                    titleDateFlagged.setText("Never");
                    titleDateFlaggedNoticeText.setVisible(true);
                }
                titleNumberRequestsText.setText(numberRequests);
            }
        });

        //add listener for selected flagged title

        flaggedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {

                //first the summary info for the flagged title is set
                if (newSelection.getFlaggedIssueNumber() > 0) {
                    RequestTitleText.setText(newSelection.getFlaggedTitleName() + " " + newSelection.getFlaggedIssueNumber());
                }
                else {
                    RequestTitleText.setText(newSelection.getFlaggedTitleName());
                }
                RequestQuantityText.setText(Integer.toString(newSelection.getFlaggedQuantity()));
                RequestNumCustomersText.setText(Integer.toString(newSelection.getFlaggedNumRequests()));

                //TODO: next load the table of customers who have requested the selected title

                //updateRequestsTable(newSelection.getTitleId());
                requestsTable.getItems().setAll(this.getRequests(newSelection.getTitleId(), newSelection.getFlaggedIssueNumber()));
            }
        });
    }

    /**
     * Private helper method to make it easier to refresh the data
     * for the reports tab
     */
    private void loadReportsTab() {
        FlaggedTitlesTotalText.setText(Integer.toString(this.getNumTitlesCurrentlyFlagged()));
        FlaggedTitlesTotalCustomersText.setText(Integer.toString((this.getNumCustomers())));
        FlaggedIssueNumbersText.setText(Integer.toString(this.getNumFlaggedWithIssueNumbers()));
        FlaggedNoRequestsText.setText(Integer.toString(getNumTitlesNoRequests()));

        flaggedTable.getItems().setAll(this.getFlaggedTitles());
    }


    /**
     * Gets a list representing all Customers in the database
     * @return An ObservableList of Customer objects
     */
    public ObservableList<Customer> getCustomers() {

        ObservableList<Customer> customers = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("select * from Customers ORDER BY LASTNAME");

            while(results.next())
            {
                int customerId = results.getInt(1);
                String firstName = results.getString(2);
                String lastName = results.getString(3);
                String phone = results.getString(4);
                String email = results.getString(5);
                customers.add(new Customer(customerId, firstName, lastName, phone, email));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return customers;
    }


    /**
     * Gets a list representing all Orders in the database.
     * @return An ObservableList of Order objects
     */
    public ObservableList<Order> getOrderTable() {
        ObservableList<Order> orders = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("SELECT ORDERS.CUSTOMERID, ORDERS.TITLEID, TITLES.title, ORDERS.QUANTITY, ORDERS.ISSUE FROM TITLES" +
                    " INNER JOIN ORDERS ON Orders.titleID=TITLES.TitleId ORDER BY TITLE");

            while(results.next())
            {
                int customerId = results.getInt(1);
                int titleId = results.getInt(2);
                String title = results.getString(3);
                int quantity = results.getInt(4);
                int issue = results.getInt(5);

                orders.add(new Order(customerId, titleId, title, quantity, issue));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return orders;
    }

    /**
     * Gets a list representing all Titles in the database
     * @return An ObeservableList of all Title objects
     */
    public ObservableList<Title> getTitles() {

        ObservableList<Title> titles  = FXCollections.observableArrayList();

        try
        {
            Statement s = conn.createStatement();
            ResultSet results = s.executeQuery("select * from Titles order by TITLE");

            while(results.next())
            {
                int titleId = results.getInt("TITLEID");
                String title = results.getString("TITLE");
                int price= results.getInt("PRICE");
                String notes = results.getString("NOTES");
                boolean flagged = results.getBoolean("FLAGGED");
                Date dateFlagged = results.getDate("DATE_FLAGGED");
                int issueFlagged = results.getInt("ISSUE_FLAGGED");
                if (dateFlagged != null) {
                    titles.add(new Title(titleId, title, price, notes, flagged, dateFlagged.toLocalDate(), issueFlagged));
                }
                else {
                    titles.add(new Title(titleId, title, price, notes, flagged, null, issueFlagged));
                }
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        for (Title t : titles) {
            t.flaggedProperty().addListener((obs, wasFlagged, isFlagged) -> {
                if (isFlagged) {
                    try {
                        System.out.println(isFlagged);
                        Statement s = conn.createStatement();
                        String sql = "SELECT * FROM ORDERS WHERE TITLEID = " + t.getId() + " AND ISSUE IS NOT NULL";
                        ResultSet results = s.executeQuery(sql);

                        if (results.next()) {
                            int titleId = results.getInt("TITLEID");
                            if (t.getId() == titleId) {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setContentText("This title has at least one issue # request.\nPlease enter the issue # for the new release.");
                                dialog.setTitle("Confirm Issue");
                                dialog.setHeaderText("");
                                final Button buttonOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                                buttonOk.addEventFilter(ActionEvent.ACTION, event -> {
                                    try {
                                        t.setIssueFlagged(Integer.parseInt(dialog.getEditor().getText()));
                                    } catch (NumberFormatException e) {
                                        event.consume();
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Please enter a valid integer", ButtonType.OK);
                                        alert.show();
                                    }
                                });
                                if (dialog.showAndWait().isEmpty()) {
                                    t.setFlagged(false);
                                }
                            }
                        }
                        results.close();
                        s.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    this.unsaved = true;
                }
            });
        }
        return titles;
    }

    public ObservableList<FlaggedTable> getFlaggedTitles() {

        ObservableList<FlaggedTable> flaggedTitles = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            s = conn.createStatement();

            ResultSet results = s.executeQuery("""
            SELECT TITLEID, TITLE, ISSUE, PRICE, SUM(QUANTITY) AS QUANTITY, COUNT(CUSTOMERID) AS NUM_REQUESTS FROM (
                                                                                                                    SELECT TITLES.TITLEID, TITLES.TITLE, ORDERS.CUSTOMERID, ORDERS.ISSUE, TITLES.PRICE, ORDERS.QUANTITY
                                                                                                                    from TITLES
                                                                                                                             LEFT JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                                                                                    WHERE TITLES.FLAGGED = true AND (ISSUE = ISSUE_FLAGGED OR ISSUE IS NULL)
                                                                                                                ) AS FLAGGED_ORDERS
            GROUP BY TITLEID, TITLE, ISSUE, PRICE
            ORDER BY TITLE, ISSUE
            """);
            
            while(results.next())
            {
                int titleId = results.getInt(1);
                String title = results.getString(2);
                int issue = results.getInt(3);
                int price= results.getInt(4);
                int quantity = results.getInt(5);
                int numRequests = results.getInt(6);

                flaggedTitles.add(new FlaggedTable( titleId, title, issue, price, quantity, numRequests));

            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return flaggedTitles;
    }

    /*
    TODO
     */
    public ObservableList<RequestTable> getRequests(int titleId, int issue) {

        ObservableList<RequestTable> requestsTable = FXCollections.observableArrayList();

        Statement s = null;
        try
        {
            String sql = "";
            if (issue > 0) {
                sql = String.format("""
                        SELECT CUSTOMERS.LASTNAME, CUSTOMERS.FIRSTNAME, ORDERS.QUANTITY FROM CUSTOMERS
                        INNER JOIN ORDERS ON ORDERS.CUSTOMERID=CUSTOMERS.CUSTOMERID
                        WHERE ORDERS.TITLEID=%s AND ORDERS.ISSUE=%s
                        ORDER BY CUSTOMERS.LASTNAME
                        """, titleId, issue);
            } else {
                sql = String.format("""
                        SELECT CUSTOMERS.LASTNAME, CUSTOMERS.FIRSTNAME, ORDERS.QUANTITY FROM CUSTOMERS
                        INNER JOIN ORDERS ON ORDERS.CUSTOMERID=CUSTOMERS.CUSTOMERID
                        WHERE ORDERS.TITLEID=%s AND ORDERS.ISSUE IS NULL
                        ORDER BY CUSTOMERS.LASTNAME
                        """, titleId);
            }

            s = conn.createStatement();

            ResultSet results = s.executeQuery(sql);

            while(results.next())
            {
                String lastName = results.getString(1);
                String firstName = results.getString(2);
                int quantity = results.getInt(3);
                requestsTable.add(new RequestTable( lastName, firstName, quantity));
            }
            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return requestsTable;
    }

    /**
     * Creates a connection to the database and sets the global conn variable.
     */
    private void createConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:" + System.getProperty("user.home") + "/DragonSlayer/derbyDB;create=true");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * helper method to get the first piece of summary info on "new week pulls" tab: the total # of flagged titles
     */
    private int getNumTitlesCurrentlyFlagged() {

        int numTitlesCurrentlyFlagged = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("SELECT COUNT(TITLES.FLAGGED) AS FlagCount FROM TITLES WHERE FLAGGED=TRUE");

            results.next();
            numTitlesCurrentlyFlagged = results.getInt("FlagCount");

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesCurrentlyFlagged;
    }


    /**
     * helper method to get the second piece of summary info on "new week pulls" tab: the number of customers the titles are flagged for
     */
    private int getNumCustomers(){
        int numCustomers = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                SELECT COUNT(*) FROM (
                        SELECT DISTINCT CUSTOMERID FROM ORDERS
                        LEFT JOIN TITLES T on ORDERS.TITLEID = T.TITLEID
                        WHERE FLAGGED = TRUE
                    ) AS FLAGGED_CUSTOMERS
            """);

            results.next();
            numCustomers = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numCustomers;
    }


    /**
     * helper method to get the third piece of summary info on "new week pulls" tab: the number of titles that have triggered issue #'s
     */
    private int getNumFlaggedWithIssueNumbers(){
        int numTitlesWithIssueNumbers = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                SELECT COUNT(*) AS TRIGGERED_ISSUE_COUNT FROM (
                    SELECT DISTINCT TITLEID FROM (
                                                     SELECT TITLES.TITLEID, ORDERS.ISSUE
                                                     FROM TITLES
                                                              INNER JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                     WHERE TITLES.FLAGGED = true AND ISSUE = ISSUE_FLAGGED
                                                 ) AS FLAGGED_ORDERS
                    ) AS ISSUE_NOT_NULL_TITLES
            """);

            results.next();
            numTitlesWithIssueNumbers = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesWithIssueNumbers;
    }


    /**
     * helper method to get the fourth piece of summary info on "new week pulls" tab: the number of titles with no customer requests
     */
    private int getNumTitlesNoRequests() {
        int numTitlesWithNoRequests = 0;

        Statement s = null;
        try
        {
            s = conn.createStatement();
            ResultSet results = s.executeQuery("""
                        SELECT COUNT(*) FROM (
                                SELECT DISTINCT T.TITLEID
                                FROM ORDERS
                                LEFT JOIN TITLES T on ORDERS.TITLEID = T.TITLEID
                                WHERE FLAGGED = TRUE
                        ) AS FLAGGED_WITH_REQUESTS
                        RIGHT JOIN TITLES ON TITLES.TITLEID = FLAGGED_WITH_REQUESTS.TITLEID
                        WHERE FLAGGED_WITH_REQUESTS.TITLEID IS NULL AND FLAGGED = TRUE
            """);

            results.next();
            numTitlesWithNoRequests = results.getInt(1);

            results.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return numTitlesWithNoRequests;
    }

    /**
     * Runs when the Add Customer button is pressed. Creates a new window for
     * the user to enter information and create a customer. Re-renders the
     * Customer table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleAddCustomer(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewCustomerBox.fxml"));
            Parent root = fxmlLoader.load();

            NewCustomerController newCustomerController = fxmlLoader.getController();
            newCustomerController.setConnection(conn);

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Add Customer");
            window.setResizable(false);
            window.setHeight(250);
            window.setWidth(400);
            window.setScene(new Scene(root));
            window.setOnHidden( e -> {
                customerTable.getItems().setAll(getCustomers());
                this.loadReportsTab();
            });

            window.show();
        } catch (Exception e) {
            System.out.println("Error when opening window. This is probably a bug");
            e.printStackTrace();
        }
    }

    /**
     * Runs when the Add Title button is pressed. Creates a new window for
     * the user to enter information and create a title. Re-renders the
     * Title table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleAddTitle(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("NewTitleBox.fxml"));
            Parent root = fxmlLoader.load();

            NewTitleController newTitleController = fxmlLoader.getController();
            newTitleController.setConnection(this.conn);

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("Add Title");
            window.setResizable(false);
            window.setHeight(285);
            window.setWidth(400);
            window.setScene(new Scene(root));
            window.setOnHidden( e -> {
                titleTable.getItems().setAll(getTitles());
                this.loadReportsTab();
            });

            window.show();
        } catch (Exception e) {
            System.out.println("Error when opening window. This is probably a bug");
            e.printStackTrace();
        }
    }

    /**
     * Runs when the Delete Customer button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Customer. It also deletes every order
     * linked to the customer. Re-renders the Customer and the order tables on window close.
     *
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteCustomer(ActionEvent event) {
        String firstName = customerFirstNameText.getText();
        String lastName = customerLastNameText.getText();

        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select a customer.");
        }
        else {
            int customerId = customerTable.getSelectionModel().getSelectedItem().getId();

            boolean confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete customer " + firstName + " " + lastName + "?");
            if (confirmDelete) {
                PreparedStatement s = null; // To prepare and execute the sql statement to delete the customer
                String sql = "DELETE FROM Customers WHERE customerId = ?";
                String sql2 = "DELETE FROM Orders WHERE customerId = ?";

                try {
                    s = conn.prepareStatement(sql2);
                    s.setString(1, Integer.toString(customerId));

                    int rowsAffected = s.executeUpdate();

                    if (rowsAffected == 0 ) {
                        //TODO: Throw an error
                    } else if (rowsAffected > 1) {
                        //TODO: Throw and error
                    }
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }

                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(customerId));

                    int rowsAffected = s.executeUpdate();

                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            customerTable.getItems().setAll(getCustomers());
            customerFirstNameText.setText("");
            customerLastNameText.setText("");
            customerPhoneText.setText("");
            customerEmailText.setText("");

            titleTable.getItems().setAll(getTitles());
            updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());


            this.loadReportsTab();
        }
    }

    /**
     * Runs when the Delete Title button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Title. It also deletes evry order
     * linked to this title. Re-renders the Title and Order table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteTitle(ActionEvent event) {
        String title = titleTitleText.getText();

        if (titleTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select a title.");
        }
        else {
            int titleId = titleTable.getSelectionModel().getSelectedItem().getId();

            boolean confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete " + title + "?");
            if (confirmDelete) {
                PreparedStatement s = null;
                String sql = "DELETE FROM TITLES WHERE TITLEID = ?";
                String sql2 = "DELETE FROM ORDERS WHERE TITLEID = ?";

                try {
                    s = conn.prepareStatement(sql2);
                    s.setString(1, Integer.toString(titleId));
                    int rowsAffected = s.executeUpdate();

                    if (rowsAffected == 0) {
                        //TODO: Throw an error
                    } else if (rowsAffected > 1) {
                        //TODO: Throw and error
                    }
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }

                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(titleId));
                    int rowsAffected = s.executeUpdate();

                    if (rowsAffected == 0) {
                        //TODO: Throw an error
                    } else if (rowsAffected > 1) {
                        //TODO: Throw and error
                    }
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            titleTable.getItems().setAll(getTitles());
            titleTitleText.setText("");
            titlePriceText.setText("");
            titleNotesText.setText("");

            titleTable.getItems().setAll(getTitles());
            updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());

            this.loadReportsTab();
        }
    }

    /**
     * Runs when the Edit Customer button is pressed. Creates a new window for
     * the user to enter information and edit a Customer. Re-renders the
     * Customer table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditCustomer(ActionEvent event) {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select a customer.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditCustomerBox.fxml"));
                Parent root = fxmlLoader.load();

                EditCustomerController editCustomerController = fxmlLoader.getController();
                editCustomerController.setConnection(conn);
                editCustomerController.setCustomer(customerTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Customer");
                window.setResizable(false);
                window.setHeight(250);
                window.setWidth(400);
                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    customerTable.getItems().setAll(getCustomers());
                    customerFirstNameText.setText("");
                    customerLastNameText.setText("");
                    customerPhoneText.setText("");
                    customerEmailText.setText("");
                    this.loadReportsTab();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Edit Title button is pressed. Creates a new window for
     * the user to enter information and edit a title. Re-renders the
     * Title table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditTitle(ActionEvent event) {
        if (titleTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select a title.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditTitleBox.fxml"));
                Parent root = fxmlLoader.load();

                EditTitleController editTitleController = fxmlLoader.getController();
                editTitleController.setConnection(conn);
                editTitleController.setTitle(titleTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Title");
                window.setResizable(false);

                window.setHeight(285);
                window.setWidth(400);

                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    titleTable.getItems().setAll(getTitles());
                    titleTitleText.setText("");
                    titlePriceText.setText("");
                    titleNotesText.setText("");
                    this.loadReportsTab();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Edit request(order) button is pressed. Creates a new window for
     * the user to enter information and edit order. Re-renders the
     * Orders table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleEditOrder(ActionEvent event) {
        if (customerOrderTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Edit", "Please select an order.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditOrderBox.fxml"));
                Parent root = fxmlLoader.load();

                EditOrderController editOrderController = fxmlLoader.getController();
                editOrderController.setConnection(conn);

                editOrderController.populate(this.getTitles());
                editOrderController.setOrder(customerOrderTable.getSelectionModel().getSelectedItem());

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edit Order");
                window.setResizable(false);

                window.setHeight(285);
                window.setWidth(400);

                window.setScene(new Scene(root));
                window.setOnHidden(e -> {
                    updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
                    this.loadReportsTab();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs when the Add Request button is pressed. Creates a new window for
     * the user to enter information and create an Order. Re-renders the
     * Orders table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleNewOrder(ActionEvent event) {
        if (customerTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("New Order", "Please select a customer.");
        }
        else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddOrderBox.fxml"));
                Parent root = fxmlLoader.load();

                NewOrderController newOrderController = fxmlLoader.getController();
                newOrderController.setConnection(conn);
                newOrderController.setCustomerID(customerTable.getSelectionModel().getSelectedItem().getId());
                newOrderController.populate(this.getTitles());
                newOrderController.setNewOrder();

                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("New Order");
                window.setResizable(false);
                window.setHeight(250);
                window.setWidth(400);
                window.setScene(new Scene(root));
                window.setOnHidden(e ->  {
                    updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
                    this.loadReportsTab();
                });
                window.show();
            } catch (Exception e) {
                System.out.println("Error when opening window. This is probably a bug");
                e.printStackTrace();
            }
        }
    }

    private Sheet createAndInitializeWorkbook(Workbook workbook) {
        LocalDate today = LocalDate.now();
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0, 8000);

        Row header = sheet.createRow(0);

        org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Date: " + today);
        header = sheet.createRow(1);
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
        sheet.setRepeatingRows(new CellRangeAddress(4,4,0,2));
        headerCell = header.createCell(0);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCell.setCellStyle(cellStyle);
        headerCell.setCellValue("All Titles with Requests and Quantities");

        sheet.getHeader().setRight("Page &P of &N");

        return sheet;
    }

    private void saveReport(File file, Workbook workbook) {
        Alert savingAlert = new Alert(Alert.AlertType.INFORMATION, "Saving Report", ButtonType.OK);
        try {
            savingAlert.setTitle("Saving");
            savingAlert.setHeaderText("");
            savingAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            savingAlert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            savingAlert.show();

            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            savingAlert.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Report saved successfully!", ButtonType.OK);
            alert.setTitle("File Saved");
            alert.setHeaderText("");
            alert.show();

        } catch (Exception e) {
            savingAlert.close();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error writing to file. Report may not have saved successfully. Make sure the file is not in use by another program.", ButtonType.OK);
            alert.setTitle("Save Error");
            alert.show();
        }
    }

    private File addFileExtension(File file) {
        int index = file.getName().lastIndexOf('.');
        if (index == -1) {
            file = new File(file.getParent(), file.getName() + ".xlsx");
        } else {
            file = new File(file.getParent(), file.getName().substring(0,index) + ".xlsx");
        }

        return file;
    }

    @FXML
    void handleExportCustomerList(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "Customer List " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Customers by Last Name");

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);

            Row row = sheet.createRow(4);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Customer");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Phone");

            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Email");

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                            SELECT * FROM CUSTOMERS
                            """;

                s = conn.createStatement();
                result = s.executeQuery(sql);
                int i = 5;
                while(result.next()) {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    String lastName = result.getString("LASTNAME");
                    String firstName = result.getString("FIRSTNAME");
                    cell.setCellValue(lastName + ", " + firstName);
                    cell = row.createCell(1);
                    cell.setCellValue(result.getString("PHONE"));
                    cell = row.createCell(2);
                    cell.setCellValue(result.getString("EMAIL"));
                    i++;
                }
                result.close();
                s.close();
                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    @FXML
    void handleExportFlaggedTitles(ActionEvent event) {
        LocalDate today = LocalDate.now();
        String fileName = "All Flagged Titles " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 6000);

            Row header = sheet.createRow(0);

            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,4));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("All Flagged Titles with Requests and Quantities");

            Row row = sheet.createRow(2);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            row = sheet.createRow(4);
            cell = row.createCell(0);
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);
            cell.setCellValue("Title");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Issue");


            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Price");

            cell = row.createCell(3);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Quantity");

            cell = row.createCell(4);
            cell.setCellStyle(headStyle);
            cell.setCellValue("# of Requests");

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setWrapText(true);
            CellStyle rightAlign = workbook.createCellStyle();
            rightAlign.setAlignment(HorizontalAlignment.RIGHT);

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                            SELECT TITLEID, TITLE, ISSUE, PRICE, SUM(QUANTITY) AS QUANTITY, COUNT(CUSTOMERID) AS NUM_REQUESTS FROM (
                                                                                                                    SELECT TITLES.TITLEID, TITLES.TITLE, ORDERS.CUSTOMERID, ORDERS.ISSUE, TITLES.PRICE, ORDERS.QUANTITY
                                                                                                                    from TITLES
                                                                                                                             LEFT JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                                                                                                    WHERE TITLES.FLAGGED = true AND (ISSUE = ISSUE_FLAGGED OR ISSUE IS NULL)
                                                                                                                ) AS FLAGGED_ORDERS
                            GROUP BY TITLEID, TITLE, ISSUE, PRICE
                            ORDER BY TITLE, ISSUE
                            """;

                s = conn.createStatement();
                result = s.executeQuery(sql);
                int i = 5;
                while(result.next()) {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    cell.setCellStyle(titleStyle);
                    cell.setCellValue(result.getString("TITLE"));

                    cell = row.createCell(1);
                    cell.setCellStyle(rightAlign);
                    Object issue = result.getObject("ISSUE");
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }

                    cell = row.createCell(2);
                    cell.setCellStyle(rightAlign);
                    int price = result.getInt("PRICE");
                    if (price != 0) {
                        cell.setCellValue(centsToDollars(price));
                    }

                    cell = row.createCell(3);
                    cell.setCellStyle(rightAlign);
                    int quantity = result.getInt("QUANTITY");
                    cell.setCellValue(quantity);

                    cell = row.createCell(4);
                    cell.setCellStyle(rightAlign);
                    cell.setCellValue(result.getInt("NUM_REQUESTS"));
                    i++;
                }
                result.close();
                s.close();

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    @FXML
    void handleExportAllTitles(ActionEvent event) {
        ObservableList<Title> titles = titleTable.getItems();
        LocalDate today = LocalDate.now();
        String fileName = "All Requests by Title " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = createAndInitializeWorkbook(workbook);

            Font bold = workbook.createFont();
            bold.setBold(true);

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            Row row = sheet.createRow(4);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue("Title");
            CellStyle titleHeadStyle = workbook.createCellStyle();
            titleHeadStyle.setFont(bold);
            cell.setCellStyle(titleHeadStyle);

            cell = row.createCell(1);
            cell.setCellValue("Issue");
            cell.setCellStyle(headStyle);

            cell = row.createCell(2);
            cell.setCellValue("Quantity");
            cell.setCellStyle(headStyle);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setWrapText(true);
            CellStyle rightAlign = workbook.createCellStyle();
            rightAlign.setAlignment(HorizontalAlignment.RIGHT);

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = """
                                SELECT TITLEID, TITLE, ISSUE, SUM(QUANTITY) AS QUANTITY FROM (
                                    SELECT TITLES.TITLEID, TITLES.TITLE, ORDERS.CUSTOMERID, ORDERS.ISSUE, TITLES.PRICE, ORDERS.QUANTITY
                                    from TITLES
                                    LEFT JOIN ORDERS ON ORDERS.TITLEID = TITLES.TITLEID
                                ) AS ORDERS
                                GROUP BY TITLEID, TITLE, ISSUE, PRICE
                                ORDER BY TITLE, ISSUE
                            """;

                s = conn.createStatement();
                result = s.executeQuery(sql);

                int i = 5;
                int totalQuantity = 0;
                int totalTitles = 0;
                while(result.next()) {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    cell.setCellStyle(titleStyle);
                    cell.setCellValue(result.getString("TITLE"));
                    cell = row.createCell(1);
                    Object issue = result.getObject("ISSUE");
                    cell.setCellStyle(rightAlign);
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }
                    cell = row.createCell(2);
                    int quantity = result.getInt("QUANTITY");
                    cell.setCellStyle(rightAlign);
                    cell.setCellValue(quantity);
                    totalQuantity += quantity;
                    totalTitles++;
                    i++;
                }

                result.close();
                s.close();

//                row = sheet.createRow(3);
//                cell = row.createCell(0);
//                cell.setCellValue("Total Titles:");
//                cell = row.createCell(1);
//                cell.setCellValue(totalTitles);
//
//                row = sheet.createRow(4);
//                cell = row.createCell(0);
//                cell.setCellValue("Total Customers:");
//                cell = row.createCell(1);
//                cell.setCellValue(totalCustomers);

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    @FXML
    void handleExportSingleCustomer(ActionEvent event) {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();

        if (customer == null) {
            Alert selectedAlert = new Alert(Alert.AlertType.INFORMATION, "Please select a customer.", ButtonType.OK);
            selectedAlert.setTitle("Confirm Export");
            selectedAlert.setHeaderText("");
            selectedAlert.show();
        }

        LocalDate today = LocalDate.now();
        String fileName = customer.getFullName() + " Requests " + today + ".xlsx";

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Location");
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

        if (file != null) {
            file = addFileExtension(file);

            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(fileName);
            sheet.setColumnWidth(0, 6000);

            Row header = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
            headerCell.setCellValue("Date: " + today);
            header = sheet.createRow(1);
            sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
            headerCell = header.createCell(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCell.setCellStyle(cellStyle);
            headerCell.setCellValue("Single Customer Request List");

            Row row = sheet.createRow(2);
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
            cell.setCellValue("Customer: " + customer.getFullName());

            Font bold = workbook.createFont();
            bold.setBold(true);
            CellStyle headStyle = workbook.createCellStyle();
            headStyle.setFont(bold);
            headStyle.setAlignment(HorizontalAlignment.RIGHT);

            row = sheet.createRow(4);

            cell = row.createCell(0);
            CellStyle reqItemHeadStyle = workbook.createCellStyle();
            reqItemHeadStyle.setFont(bold);
            cell.setCellStyle(reqItemHeadStyle);
            cell.setCellValue("Requested Item");

            cell = row.createCell(1);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Issue");

            cell = row.createCell(2);
            cell.setCellStyle(headStyle);
            cell.setCellValue("Quantity");

            CellStyle reqItemStyle = workbook.createCellStyle();
            reqItemStyle.setWrapText(true);
            CellStyle rightAlign = workbook.createCellStyle();
            rightAlign.setAlignment(HorizontalAlignment.RIGHT);

            ResultSet result;
            Statement s = null;
            try
            {
                String sql = String.format("""
                        SELECT ORDERS.CUSTOMERID, ORDERS.TITLEID, TITLES.title, ORDERS.QUANTITY, ORDERS.ISSUE FROM TITLES
                        INNER JOIN ORDERS ON Orders.titleID=TITLES.TitleId
                        WHERE ORDERS.CUSTOMERID=%s
                        ORDER BY TITLE
                        """, customer.getId());

                s = conn.createStatement();
                ResultSet results = s.executeQuery(sql);

                int i = 5;
                int totalQuantity = 0;
                while(results.next())
                {
                    row = sheet.createRow(i);
                    cell = row.createCell(0);
                    cell.setCellStyle(reqItemStyle);
                    cell.setCellValue(results.getString("TITLE"));
                    cell = row.createCell(1);
                    Object issue = results.getObject("ISSUE");
                    if (issue != null) {
                        cell.setCellValue(Integer.parseInt(issue.toString()));
                    } else {
                        cell.setCellValue("All");
                    }
                    cell.setCellStyle(rightAlign);
                    cell = row.createCell(2);
                    int quantity = results.getInt("QUANTITY");
                    cell.setCellValue(quantity);
                    cell.setCellStyle(rightAlign);
                    totalQuantity += quantity;
                    i++;
                }
                results.close();
                s.close();

                row = sheet.createRow(i+1);
                cell = row.createCell(0);
                cell.setCellValue("Total:");
                cell = row.createCell(2);
                cell.setCellValue(totalQuantity);

                saveReport(file, workbook);
            }
            catch (SQLException sqlExcept)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                alert.setTitle("Database Error");
                alert.show();
            }
        }
    }

    @FXML
    void handleExportSingleTitleFlaggedTable(ActionEvent event) {
        FlaggedTable flaggedTableTitle = flaggedTable.getSelectionModel().getSelectedItem();
        Title title = new Title(flaggedTableTitle.getTitleId(), flaggedTableTitle.getFlaggedTitleName(), flaggedTableTitle.getFlaggedPriceCents(), "");
        exportSingleTitle(event, title);
    }

    /**
     * This method is called when the singleTitleReportButton button is
     * clicked.
     */
    @FXML
    void handleExportSingleTitle(ActionEvent event) {

        Title title = titleTable.getSelectionModel().getSelectedItem();
        exportSingleTitle(event, title);
    }

    private void exportSingleTitle(ActionEvent event, Title title) {
        if (title == null) {
            Alert selectedAlert = new Alert(Alert.AlertType.INFORMATION, "Please select a title.", ButtonType.OK);
            selectedAlert.setTitle("Confirm Export");
            selectedAlert.setHeaderText("");
            selectedAlert.show();
        } else {

            LocalDate today = LocalDate.now();
            String fileName = title.getTitle() + " Requests " + today + ".xlsx";

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Location");
            fileChooser.setInitialFileName(fileName);
            File file = fileChooser.showSaveDialog(((Node) event.getTarget()).getScene().getWindow());

            if (file != null) {
                file =  addFileExtension(file);

                Workbook workbook = new XSSFWorkbook();

                Sheet sheet = workbook.createSheet(fileName);
                sheet.setColumnWidth(0, 6000);

                Row header = sheet.createRow(0);

                org.apache.poi.ss.usermodel.Cell headerCell = header.createCell(0);
                headerCell.setCellValue("Date: " + today);
                header = sheet.createRow(1);
                sheet.addMergedRegion(new CellRangeAddress(1,1,0,2));
                headerCell = header.createCell(0);
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                headerCell.setCellStyle(cellStyle);
                headerCell.setCellValue("Single Title Customer List");

                Row row = sheet.createRow(2);
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
                cell.setCellValue("Title: " + title.getTitle());

                Font bold = workbook.createFont();
                bold.setBold(true);
                CellStyle headStyle = workbook.createCellStyle();
                headStyle.setFont(bold);
                headStyle.setAlignment(HorizontalAlignment.RIGHT);

                row = sheet.createRow(4);
                cell = row.createCell(0);
                CellStyle customerHeadStyle = workbook.createCellStyle();
                customerHeadStyle.setFont(bold);
                cell.setCellStyle(customerHeadStyle);
                cell.setCellValue("Customer");

                cell = row.createCell(1);
                cell.setCellStyle(headStyle);
                cell.setCellValue("Issue");

                cell = row.createCell(2);
                cell.setCellStyle(headStyle);
                cell.setCellValue("Quantity");

                CellStyle reqItemStyle = workbook.createCellStyle();
                reqItemStyle.setWrapText(true);
                CellStyle rightAlign = workbook.createCellStyle();
                rightAlign.setAlignment(HorizontalAlignment.RIGHT);

                ResultSet result;
                Statement s = null;
                try
                {
                    String sql = String.format("""
                        SELECT FIRSTNAME, LASTNAME, ISSUE, QUANTITY FROM ORDERS
                        LEFT JOIN CUSTOMERS C on C.CUSTOMERID = ORDERS.CUSTOMERID
                        WHERE TITLEID = %s
                        ORDER BY LASTNAME
                        """, title.getId());

                    s = conn.createStatement();
                    result = s.executeQuery(sql);
                    int i = 5;
                    int totalQuantity = 0;
                    while(result.next()) {
                        row = sheet.createRow(i);

                        cell = row.createCell(0);
                        cell.setCellValue(result.getString("LASTNAME") + " " + result.getString("FIRSTNAME"));

                        cell = row.createCell(1);
                        Object issue = result.getObject("ISSUE");
                        if (issue != null) {
                            cell.setCellValue(Integer.parseInt(issue.toString()));
                        } else {
                            cell.setCellValue("All");
                        }
                        cell.setCellStyle(rightAlign);

                        cell = row.createCell(2);
                        int quantity = result.getInt("QUANTITY");
                        cell.setCellValue(quantity);
                        cell.setCellStyle(rightAlign);

                        totalQuantity += quantity;
                        i++;
                    }
                    result.close();
                    s.close();

                    row = sheet.createRow(i+1);
                    cell = row.createCell(0);
                    cell.setCellValue("Total:");
                    cell = row.createCell(2);
                    cell.setCellValue(totalQuantity);

                    saveReport(file, workbook);
                }
                catch (SQLException sqlExcept)
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error. Report may not have saved successfully", ButtonType.OK);
                    alert.setTitle("Database Error");
                    alert.show();
                }
            }
        }
    }

    /**
     * Adds all orders for a given Customer to the Orders table.
     * @param customer The Customer to update the Order Table for
     */
    void updateOrdersTable(Customer customer){
        ObservableList<Order> allOrders = getOrderTable();
        ObservableList<Order> customerOrders = FXCollections.observableArrayList();
        for(int i=0; i < allOrders.size(); i++) {
            if (allOrders.get(i).getCustomerId() == customer.getId())
                customerOrders.add(allOrders.get(i));
        }
        customerOrderTable.getItems().setAll(customerOrders);
    }

    /**
     * Runs when the Delete Request button is pressed. Creates a dialog for the
     * user to confirm deletion of the selected Order. Re-renders the Order
     * table on window close.
     * @param event Event that triggered the method call.
     */
    @FXML
    void handleDeleteOrder(ActionEvent event) {
        String title = titleTitleText.getText();

        if (customerOrderTable.getSelectionModel().getSelectedItem() == null) {
            AlertBox.display("Confirm Delete", "Please select an order.");
        } else {
            int customerId = customerOrderTable.getSelectionModel().getSelectedItem().getCustomerId();
            int titleId = customerOrderTable.getSelectionModel().getSelectedItem().getTitleId();
            int quantity = customerOrderTable.getSelectionModel().getSelectedItem().getQuantity();
            int issue = customerOrderTable.getSelectionModel().getSelectedItem().getIssue();


            boolean confirmDelete = ConfirmBox.display(
                    "Confirm Delete",
                    "Are you sure you would like to delete " + title + "?");
            if (confirmDelete) {
                PreparedStatement s = null;
                String sql = "DELETE FROM ORDERS WHERE CUSTOMERID = ? AND TITLEID = ? AND QUANTITY = ? AND ISSUE = ?";
                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(customerId));
                    s.setString(2, Integer.toString(titleId));
                    s.setString(3, Integer.toString(quantity));
                    s.setString(4, Integer.toString(issue));
                    int rowsAffected = s.executeUpdate();

                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            titleTable.getItems().setAll(getTitles());
            updateOrdersTable(customerTable.getSelectionModel().getSelectedItem());
            this.loadReportsTab();
        }
    }

    /**
     * Saves the current state and date of all New Release Flags to the database
     */
    @FXML
    void saveFlags() {

        ObservableList<Title> titles = titleTable.getItems();
        ZonedDateTime startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        long todayMillis = startOfToday.toEpochSecond() * 1000;
        Date today = new Date(todayMillis);

        Alert savingAlert = new Alert(Alert.AlertType.INFORMATION, "Saving New Release Flags...", ButtonType.OK);

        savingAlert.setTitle("Saving");
        savingAlert.setHeaderText("");
        savingAlert.setContentText("Saving New Release Flags...");
        savingAlert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        savingAlert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
        savingAlert.show();

        for (int i = 0; i < titles.size(); i++) {
            PreparedStatement s = null;
            if (titles.get(i).isFlagged()) {
                String sql = """
                    UPDATE Titles
                    SET FLAGGED = TRUE, DATE_FLAGGED = ?, ISSUE_FLAGGED = ?
                    WHERE TITLEID = ?
                    """;
                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, DateFormat.getDateInstance().format(today));
                    if (titles.get(i).getIssueFlagged() == 0) {
                        s.setString(2, null);
                    } else {
                        s.setString(2, Integer.toString(titles.get(i).getIssueFlagged()));
                    }
                    s.setString(3, Integer.toString(titles.get(i).getId()));
                    s.executeUpdate();
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }
            else {
                String sql = """
                    UPDATE Titles
                    SET FLAGGED = FALSE, ISSUE_FLAGGED = NULL
                    WHERE TITLEID = ?
                    """;
                try {
                    s = conn.prepareStatement(sql);
                    s.setString(1, Integer.toString(titles.get(i).getId()));
                    s.executeUpdate();
                    s.close();
                } catch (SQLException sqlExcept) {
                    sqlExcept.printStackTrace();
                }
            }

        }

        savingAlert.close();

        Alert savedAlert = new Alert(Alert.AlertType.INFORMATION, "Saved Flags!", ButtonType.OK);
        savedAlert.setHeaderText("");
        savedAlert.show();
        this.unsaved = false;
        titleTable.getItems().setAll(getTitles());
        this.loadReportsTab();
    }

    /**
     * Sets the Flagged attribute of all Titles to false
     */
    @FXML
    void resetFlags() {
        Alert resetAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset all flags?" +
                " This cannot be undone.");
        resetAlert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    PreparedStatement s = null;
                    String sql = """
                                UPDATE Titles
                                SET FLAGGED = FALSE, ISSUE_FLAGGED = NULL
                                """;
                    try {
                        s = conn.prepareStatement(sql);
                        s.executeUpdate();
                        s.close();
                    } catch (SQLException sqlExcept) {
                        sqlExcept.printStackTrace();
                    }
                    titleTable.getItems().setAll(getTitles());
                    this.loadReportsTab();
                });
        this.unsaved = false;
    }

    /**
     * Gets the number of Orders for a specified Title
     * @param titleId The title to count orders for
     * @return The number of orders
     */
    private int getNumberRequests(int titleId) {
        int ordersCount = 0;
        ResultSet result;
        Statement s = null;
        try
        {
            String sql = String.format("""
                    SELECT COUNT(*) FROM ORDERS
                    WHERE titleID = %s
                    """, titleId);

            s = conn.createStatement();
            result = s.executeQuery(sql);
            while(result.next()) {
                ordersCount = result.getInt(1);
            }
            result.close();
            s.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }

        return ordersCount;
    }

    /**
     * Returns true or false based on if there are unsaved changes to New
     * Release Flags or not.
     * @return A boolean for whether or not there are unsaved changes
     */
    public boolean isUnsaved() {
        return unsaved;

    }

    private String centsToDollars(int price) {
        String total;
        int dollars = (price / 100);
        int cents = (price % 100);
        if ((cents / 10) == 0) {
            total = Integer.toString(dollars) + ".0" + Integer.toString(cents);
        }
        else {
            total = Integer.toString(dollars) + '.' + Integer.toString(cents);
        }
        return total;
    }
}