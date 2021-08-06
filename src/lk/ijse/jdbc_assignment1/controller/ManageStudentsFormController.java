package lk.ijse.jdbc_assignment1.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import lk.ijse.jdbc_assignment1.model.ContactLM;
import lk.ijse.jdbc_assignment1.model.Provider;
import lk.ijse.jdbc_assignment1.model.StudentTM;
import lk.ijse.jdbc_assignment1.util.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageStudentsFormController {
    public Button btnClear;
    public Label lblStudentID;
    public TextField txtName;
    public TextField txtContact;
    public ListView<ContactLM> lstContacts;
    public Button btnRemove;
    public TableView<StudentTM> tblStudents;
    public ComboBox<Provider> cmbProviders;
    public Button btnAdd;
    private Connection connection;
    private PreparedStatement pstmSaveStudent;
    private PreparedStatement pstmSaveContact;
    private PreparedStatement pstmDeleteStudent;
    private PreparedStatement pstmDeleteContacts;
    private PreparedStatement pstmSelectContacts;

    public void initialize() {
        lblStudentID.setText("Generated ID");
        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<StudentTM, ListView<String>> colContacts = (TableColumn<StudentTM, ListView<String>>) tblStudents.getColumns().get(2);

        cmbProviders.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnAdd.fire();
            }
        });

        colContacts.setCellValueFactory(param -> {
            ListView<String> lstContacts = new ListView<>();
            lstContacts.setPrefHeight(75);
            StudentTM student = param.getValue();
            lstContacts.setItems(FXCollections.observableArrayList(student.getContacts()));
            return new ReadOnlyObjectWrapper<>(lstContacts);
        });

        TableColumn<StudentTM, Button> colDelete = (TableColumn<StudentTM, Button>) tblStudents.getColumns().get(3);

        colDelete.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");

            btnDelete.setOnAction(event -> {

                try {
                    connection.setAutoCommit(false);
                    pstmSelectContacts.setInt(1, param.getValue().getId());

                    if (pstmSelectContacts.executeQuery().next()){

                        pstmDeleteContacts.setInt(1, param.getValue().getId());
                        int affectedRows = pstmDeleteContacts.executeUpdate();
                        if (affectedRows == 0){
                            throw new RuntimeException("Failed to delete contacts");
                        }
                    }

                    pstmDeleteStudent.setInt(1, param.getValue().getId());
                    if (pstmDeleteStudent.executeUpdate() != 1){
                        throw new RuntimeException("Failed to delete the student");
                    }

                    connection.commit();
                    new Alert(Alert.AlertType.INFORMATION, param.getValue().getId() + " has been deleted successfully").show();
                    tblStudents.getItems().remove(param.getValue());

                } catch (Throwable e) {
                    e.printStackTrace();
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    new Alert(Alert.AlertType.ERROR, "Failed to delete the student").show();
                }finally{
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        try {
            connection = DBConnection.getInstance().getConnection();
            pstmSaveStudent = connection.prepareStatement("INSERT INTO student (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            pstmSaveContact = connection.prepareStatement("INSERT INTO contact (contact, student_id, provider_id) VALUES (?,?,?);");
            pstmDeleteStudent = connection.prepareStatement("DELETE FROM student WHERE id=?");
            pstmDeleteContacts = connection.prepareStatement("DELETE FROM contact WHERE student_id=?");
            pstmSelectContacts = connection.prepareStatement("SELECT * FROM contact WHERE student_id=?");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadAllStudents();
        loadAllProviders();
    }

    private void loadAllProviders() {
        cmbProviders.getItems().clear();

        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM provider");

            while (rst.next()) {
                cmbProviders.getItems().add(new Provider(rst.getInt(1), rst.getString("provider")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAllStudents() {

        tblStudents.getItems().clear();

        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.
                    executeQuery("SELECT s.id, s.name, CONCAT(c.contact,'-',p.provider) AS contact FROM student s " +
                            "LEFT OUTER JOIN contact c on s.id = c.student_id" +
                            " LEFT OUTER JOIN provider p on c.provider_id = p.id;");

            while (rst.next()){
                int id = rst.getInt("id");
                String name = rst.getString("name");
                String contact = rst.getString("contact");

                List<String> contacts;
                if ((contacts = getStudentContactList(id)) == null) {
                    contacts= new ArrayList<>();

                    if (contact != null) {
                        contacts.add(contact);
                    }

                    tblStudents.getItems().add(new StudentTM(id, name, contacts));
                }else{
                    contacts.add(contact);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private List<String> getStudentContactList(int id){
        for (StudentTM student : tblStudents.getItems()) {
            if (student.getId() == id) return student.getContacts();
        }
        return null;
    }

    public void btnRemove_OnAction(ActionEvent actionEvent) {
        if (lstContacts.getSelectionModel().isEmpty()) return;
        lstContacts.getItems().remove(lstContacts.getSelectionModel().getSelectedItem());
    }

    public void btnClear_OnAction(ActionEvent actionEvent) {
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        try {

            // Let's start to buffer
            connection.setAutoCommit(false);

            pstmSaveStudent.setString(1, txtName.getText());
            int affectedRows = pstmSaveStudent.executeUpdate();

            if (affectedRows != 1) {
                throw new RuntimeException("Failed to save in the student table");
            }

            ResultSet generatedKeys = pstmSaveStudent.getGeneratedKeys();
            generatedKeys.next();

            /*PreparedStatement pstmQueryProviderID = connection.prepareStatement("SELECT id FROM provider WHERE provider=?");

            for (String contact : lstContacts.getItems()) {
                String[] split = contact.split("-");
                pstmSaveContact.setString(1, split[0]);
                pstmSaveContact.setInt(2, generatedKeys.getInt(1));
                pstmQueryProviderID.setString(1, split[1]);

                ResultSet rst = pstmQueryProviderID.executeQuery();
                rst.next();

                pstmSaveContact.setInt(3, rst.getInt(1));

                if (split[0].contains("a")) {
                    throw new RuntimeException("Invalid contact");
                }

                affectedRows = pstmSaveContact.executeUpdate();

                if (affectedRows != 1) {
                    throw new RuntimeException("Failed to save the contact " + contact);
                }
            }*/

            //ArrayList<String> contact = new ArrayList<>();

            for (ContactLM contacts : lstContacts.getItems()) {
                pstmSaveContact.setString(1, contacts.getContact());
                pstmSaveContact.setInt(2, generatedKeys.getInt(1));
                pstmSaveContact.setInt(3, contacts.getProviderId());
                //contact.add(contacts.getContact());

                int affectedProvidersRows = pstmSaveContact.executeUpdate();

                if (affectedProvidersRows != 1) {
                    throw new RuntimeException("Saved failed");
                }
            }


            connection.commit();
            // Buffer -> Flush
            // By default transaction apply (it doesn't buffer anymore)

            List<String> contacts = lstContacts.getItems().stream().map(contactLM -> contactLM.getContact() + "-" + contactLM.getProviderDescription()).collect(Collectors.toList());

            tblStudents.getItems().add(new StudentTM(generatedKeys.getInt(1), txtName.getText(), contacts));
            new Alert(Alert.AlertType.INFORMATION, "Student has been saved successfully").show();
            txtName.clear();
            txtContact.clear();
            lstContacts.getItems().clear();
            txtName.requestFocus();

        } catch (Throwable e) {

            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save the student").show();
        }finally{
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnBack_OnAction(ActionEvent actionEvent) throws IOException {
        HomeFormController.navigate(HomeFormController.NavigationMenu.HOME);
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {
        String contact = txtContact.getText();

        if (contact.trim().isEmpty() || cmbProviders.getSelectionModel().isEmpty()) {
            return;
        }

        Provider provider = cmbProviders.getSelectionModel().getSelectedItem();
        lstContacts.getItems().add(new ContactLM(contact, provider.getId(), provider.getProvider()));
        txtContact.clear();
        cmbProviders.getSelectionModel().clearSelection();
        txtContact.requestFocus();
    }
}
