package it.polimi.tiw.projects.dao;

import it.polimi.tiw.projects.beans.AddressBook;
import it.polimi.tiw.projects.beans.BankAccount;
import it.polimi.tiw.projects.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddressBookDAO {

    private Connection connection;

    public AddressBookDAO(Connection connection) {
        this.connection = connection;
    }

    public List<User> getAllByUser(int id) throws SQLException {
        List<User> addressBook = new ArrayList<>();
        String query = "SELECT * FROM addressbook INNER JOIN user on addressbook.owner = user.id WHERE owner = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1,id);
            //try with resources, the resultset is automatically closed after the try block
            try (ResultSet result = pstatement.executeQuery()) {
                while (result.next())
                {
                    User user = new User();
                    user.setId(result.getInt("contact"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    user.setEmail(result.getString("e-mail"));
                    addressBook.add(user);
                }
            }
        }
        return addressBook;
    }

    public void createContact(int ownerId, int contactId) throws SQLException {
        String query = "INSERT into addressbook (owner, contact) VALUES(?, ?)";
     
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, ownerId);
            pstatement.setInt(2, contactId);
            pstatement.executeUpdate();
        }
    }

    /**
     * ritorna falso se non c'è il contatto in rubrica
     * @param ownerID
     * @param contactID
     * @return
     * @throws SQLException
     */
    public boolean checkContact(int ownerID, int contactID) throws SQLException {
        String query = "SELECT * FROM addressbook WHERE owner = ? && contact = ?;";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1,ownerID);
            pstatement.setInt(2,contactID);
            //try with resources, the resultset is automatically closed after the try block
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.next())
                    return true;
            }
        }
        return false;
    }


}
