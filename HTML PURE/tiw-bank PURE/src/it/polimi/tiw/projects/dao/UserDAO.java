package it.polimi.tiw.projects.dao;

import it.polimi.tiw.projects.beans.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

import org.apache.commons.lang.ArrayUtils;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User getUserByID(int id) throws SQLException {
		String query = "SELECT  id, username, name, surname FROM user WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, String.valueOf(id));
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}

	public User checkCredentials(String username, String password) throws SQLException, NoSuchAlgorithmException {

		String query = "SELECT salt FROM user JOIN salt ON user.id = salt.user WHERE username = ?";
		String saltString = null;

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					saltString = result.getString("salt");
				}
			}
		}

		query = "SELECT  id, username, name, surname FROM user WHERE username = ? AND password =?";

		byte[] salt = Base64.getDecoder().decode(saltString);
		byte[] saltedpwd = ArrayUtils.addAll(password.getBytes(StandardCharsets.UTF_8), salt);

		MessageDigest pwdhash = MessageDigest.getInstance("SHA-256");
		byte[] hash = pwdhash.digest(saltedpwd);
		String encodedhash = Base64.getEncoder().encodeToString(hash);

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, encodedhash);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					return user;
				}
			}
		}
	}
}