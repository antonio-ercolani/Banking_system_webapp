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
import java.util.Random;

import javax.servlet.Registration;

import org.apache.commons.lang.ArrayUtils;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User getUserByID(int id) throws SQLException {
		String query = "SELECT  id, username, name, surname, `e-mail` FROM user WHERE id = ?";
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
					user.setEmail(result.getString("e-mail"));
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
		
		query = "SELECT  id, username, name, surname, `e-mail` FROM user WHERE username = ? AND password =?";
		
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
					user.setEmail(result.getString("e-mail"));
					return user;
				}
			}
		}
	}

	public void createUser(String username, String password, String name, String surname, String email)
			throws SQLException, NoSuchAlgorithmException {

		connection.setAutoCommit(false);

		String query = "INSERT into user (username, password, name, surname, `e-mail`) VALUES(?, ?, ?, ?, ?)";

		byte[] salt = createSalt();
		byte[] saltedpwd = ArrayUtils.addAll(password.getBytes(StandardCharsets.UTF_8), salt);

		MessageDigest pwdhash = MessageDigest.getInstance("SHA-256");
		byte[] hash = pwdhash.digest(saltedpwd);
		String encodedhash = Base64.getEncoder().encodeToString(hash);

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, encodedhash);
			pstatement.setString(3, name);
			pstatement.setString(4, surname);
			pstatement.setString(5, email);
			pstatement.executeUpdate();
		} catch (SQLException exc) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw exc;
		}
		
		query = "SELECT id FROM user WHERE username = ?";
		int id = 0;
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
					result.next();
					id = result.getInt("id");
				}
			} catch (SQLException exc) {
				connection.rollback();
				connection.setAutoCommit(true);
				throw exc;
			}


		query = "INSERT into salt (user, salt) VALUES(?, ?)";

		String encodedsalt = Base64.getEncoder().encodeToString(salt);
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, id);
			pstatement.setString(2, encodedsalt);
			pstatement.executeUpdate();
		} catch (SQLException exc) {
			connection.rollback();
			throw exc;
		} finally {
			connection.setAutoCommit(true);
		}
		
	}

	public boolean checkUniqueness(String username) {
		String query = "SELECT * FROM user WHERE username = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				return !result.isBeforeFirst();
			}
		} catch (SQLException e) {
			return false;
		}
	}
	
	private byte[] createSalt() {
		byte[] salt = new byte[32];
		Random random = new Random();
		random.nextBytes(salt);
		return salt;
	}
}