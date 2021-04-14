package it.polimi.tiw.projects.dao;

import it.polimi.tiw.projects.beans.BankAccount;
import it.polimi.tiw.projects.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankAccountDAO {

    private Connection connection;

    public BankAccountDAO(Connection connection) {
        this.connection = connection;
    }


    public List<BankAccount> getBankAccountsByUser(int userID) throws SQLException {
        List<BankAccount> bankAccounts = new ArrayList<>();
        String query = "SELECT * FROM bankAccount WHERE user = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, String.valueOf(userID));
            //try with resources, the resultset is automatically closed after the try block
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next())
                {
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setId(result.getInt("id"));
                    bankAccount.setBalance(result.getFloat("balance"));
                    bankAccounts.add(bankAccount);
                }
            }
        }
        return  bankAccounts;
    }

    public User getUserbyBankAccount(int bankID) throws SQLException {
        User user = null;
        String query = "SELECT user.id, user.username, user.name, user.surname FROM user INNER JOIN bankaccount ON bankaccount.user = user.id WHERE bankaccount.id = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, String.valueOf(bankID));
            //try with resources, the resultset is automatically closed after the try block
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next())
                {
                    user = new User();
                    user.setId(result.getInt("id"));
                    user.setUsername(result.getString("username"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                }
            }
        }
        return user;
    }

    public Float getBalance(int bankID) throws SQLException {
        Float balance = null;
        String query = "SELECT balance FROM bankaccount WHERE bankaccount.id = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, String.valueOf(bankID));
            //try with resources, the resultset is automatically closed after the try block
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next())
                    balance = result.getFloat("balance");
            }
        }
        return balance;
    }


}
