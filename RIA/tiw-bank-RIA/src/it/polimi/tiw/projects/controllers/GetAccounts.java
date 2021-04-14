package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.projects.beans.BankAccount;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

@WebServlet("/GetAccounts")
public class GetAccounts extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	

	
	public GetAccounts() {
        super();
    }
	
	public void init() throws ServletException {
		// initialize database connection
   		connection = ConnectionHandler.getConnection(getServletContext());
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		BankAccountDAO bDAO = new BankAccountDAO(connection);
		List<BankAccount> accounts = null;
		try {
			accounts = bDAO.getBankAccountsByUser(user.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in worker's project database extraction");
		}
		
		String json = new Gson().toJson(accounts);
		
		response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
