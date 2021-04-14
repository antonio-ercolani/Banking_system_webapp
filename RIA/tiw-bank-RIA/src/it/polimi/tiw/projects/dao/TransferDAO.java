package it.polimi.tiw.projects.dao;

import it.polimi.tiw.projects.beans.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransferDAO {
	private Connection connection;

	public TransferDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Transfer> getAllbyBankAccount(int bank_id) throws SQLException {
		List<Transfer> transfers = new ArrayList<>();
		String query = "SELECT * FROM transfer WHERE source = ? OR destination = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, bank_id);
			pstatement.setInt(2, bank_id);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Transfer transfer = new Transfer();
					transfer.setId(result.getInt("id"));
					transfer.setDate(result.getDate("date"));
					transfer.setAmount(result.getDouble("amount"));
					transfer.setSource(result.getInt("source"));
					transfer.setDestination(result.getInt("destination"));
					transfers.add(transfer);
				}
			}
		}
		
		return transfers;
	}
	
	
	public void createTransfer(int source, int destination, double amount) throws SQLException {
		
		String query = "INSERT into transfer (date, amount, source, destination) VALUES(?, ?, ?, ?)";
		
		connection.setAutoCommit(false);
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setDate(1, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
			pstatement.setDouble(2, amount);
			pstatement.setInt(3, source);
			pstatement.setInt(4, destination);
			pstatement.executeUpdate();
		} catch (SQLException exc) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw exc;
		}

		query = "UPDATE bankaccount " +
				"SET balance = balance + ? " +
				"WHERE id = ?;   ";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setDouble(1, amount);
			pstatement.setInt(2, destination);
			pstatement.executeUpdate();
		} catch (SQLException exc) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw exc;
		}

		query = "UPDATE bankaccount " +
				"SET balance = balance - ? " +
				"WHERE id = ?;   ";

		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setDouble(1, amount);
			pstatement.setInt(2, source);
			pstatement.executeUpdate();
			connection.commit();
			
		} catch (SQLException exc) {
			connection.rollback();
			throw exc;
		} finally {
			connection.setAutoCommit(true);
		}

	}
}
