package lk.ijse.jdbc_assignment1.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.jdbc_assignment1.tm.ProviderTM;

import java.io.IOException;
import java.sql.*;

public class ManageProvidersFormController {
    public TextField txtID;
    public TextField txtProvider;
    public Button btnSave;
    public TableView<ProviderTM> tblProviders;

    private Connection connection;
    private PreparedStatement pstmProvider;
    private PreparedStatement pstmDeleteProvider;

    public void initialize() {
        tblProviders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblProviders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("provider"));
        TableColumn<ProviderTM, Button> colDelete = (TableColumn<ProviderTM, Button>) tblProviders.getColumns().get(2);

        colDelete.setCellValueFactory(param -> {
            Button btnDelete = new Button("Remove");

            btnDelete.setOnAction(event -> {
                try {
                    pstmDeleteProvider.setInt(1,param.getValue().getId());
                    if (pstmDeleteProvider.executeUpdate() == 1){
                        new Alert(Alert.AlertType.INFORMATION,param.getValue().getId()+"has been deleted succssfully").show();
                        tblProviders.getItems().remove(param.getValue());
                    }else {
                        throw new RuntimeException("Failed to delete provider");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        txtProvider.setOnAction(this::btnSave_OnAction);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep7","root","mysql");
            pstmProvider = connection.prepareStatement("INSERT INTO provider (id, provider) VALUES (?,?);");
            pstmDeleteProvider = connection.prepareStatement("DELETE FROM provider WHERE id=?;");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        loadAllProviders();
    }

    private void loadAllProviders(){
        tblProviders.getItems().clear();

        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM provider;");

            while (rst.next()){
                tblProviders.getItems().add(new ProviderTM(rst.getInt(1),rst.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void btnBack_OnAction(ActionEvent actionEvent) throws IOException {
        HomeFormController.navigate(HomeFormController.NavigationMenu.HOME);
    }

    public void btnSave_OnAction(ActionEvent actionEvent) {
        String provider = txtProvider.getText();

        if (txtID.getText().trim().isEmpty() || provider.trim().isEmpty() || !txtID.getText().matches("\\d")){
            new Alert(Alert.AlertType.ERROR,"Please enter valid details").show();
            return;
        }

        try {
            pstmProvider.setInt(1,Integer.parseInt(txtID.getText()));
            pstmProvider.setString(2,provider);

            if (pstmProvider.executeUpdate() == 1){
                new Alert(Alert.AlertType.INFORMATION,"Provider saved successfully").show();
                tblProviders.getItems().add(new ProviderTM(Integer.parseInt(txtID.getText()),provider));
                txtID.clear();
                txtProvider.clear();
                txtID.requestFocus();
            }else {
                throw new RuntimeException("Failed to save provider");
            }
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,e.getMessage()).show();
        }

    }
}
