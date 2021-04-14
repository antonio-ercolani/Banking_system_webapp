package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.projects.beans.Transfer;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetTransfers")
@MultipartConfig
public class GetTransfers extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetTransfers() {
		super();
	}

	public void init() throws ServletException {
		// initialize database connection
   		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		int bankAccountId = (Integer) session.getAttribute("bank_id");

		// get account transfers list
		TransferDAO transferDAO = new TransferDAO(connection);
		List<Transfer> transfers = new ArrayList<Transfer>();
		try {
			transfers = transferDAO.getAllbyBankAccount(bankAccountId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover account balance");
			return;
		}
		
		String json = new Gson().toJson(transfers);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
		
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
