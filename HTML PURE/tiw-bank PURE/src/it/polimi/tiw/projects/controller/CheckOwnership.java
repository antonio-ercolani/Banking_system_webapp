package it.polimi.tiw.projects.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;


@WebServlet("/CheckOwnership")
public class CheckOwnership extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public CheckOwnership() {
		super();
    }

    public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int bankAccountId = 0;
		boolean badRequest;
		try {
			bankAccountId = Integer.parseInt(request.getParameter("bank_id"));
		} catch (NumberFormatException e) {
			
		}
		BankAccountDAO bDAO = new BankAccountDAO(connection);
		HttpSession session = request.getSession();
		String path = getServletContext().getContextPath();

		User accountOwner = null;
		try {
			accountOwner = bDAO.getUserbyBankAccount(bankAccountId);
		} catch (SQLException e) {
			badRequest = true;
		}
		User currentUser = (User) session.getAttribute("user");
		if (accountOwner != null && currentUser != null && accountOwner.getId() == currentUser.getId() ) {
			badRequest = false;
			session.setAttribute("bank_id", bankAccountId);
		} else {
			badRequest = true;
		}
		if (badRequest) {
			path = path + "/GoToHomePage";
		} else {
			path = path + "/GoToAccountInfo";
		}
		response.sendRedirect(path);
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
