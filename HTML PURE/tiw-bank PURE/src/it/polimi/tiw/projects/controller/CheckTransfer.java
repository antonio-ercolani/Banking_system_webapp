package it.polimi.tiw.projects.controller;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.ErrorType;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@WebServlet("/CheckTransfer")
public class CheckTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public CheckTransfer() {
		super();
	}

	public void init() throws ServletException {
		ServletContext context = getServletContext();

		// initialize database connection
		connection = ConnectionHandler.getConnection(context);

		// initialize template engine
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		boolean badRequest = false;
		int sourceAccount = 0;
		int userDestinationId = 0;
		int destinationAccount = 0;
		double amount = 0;
		String description = null;
		HttpSession session = request.getSession();
		
		ErrorType errorType = ErrorType.checkOk;
		//ERROR LEGEND --> SEE TransferErrorPage.html 
		
		try {
			sourceAccount = (Integer)(session.getAttribute("bank_id"));
			userDestinationId = Integer.parseInt(request.getParameter("userDest"));
			destinationAccount = Integer.parseInt(request.getParameter("destination"));
			amount = Double.parseDouble(request.getParameter("amount"));
			description = request.getParameter("description");
		} catch (NumberFormatException | NullPointerException e) {
			badRequest = true;
		}
		if (badRequest) {
			errorType = ErrorType.wrongFormat;
		} else {
			if (amount < 0) {
				errorType = ErrorType.negativeAmount;
			} else {
				try {
					if (!checkBalanceValidity(sourceAccount, amount, response))
						errorType = ErrorType.insufficientBalance;
					
					else if (!checkDestinationUserExist(userDestinationId))
						errorType = ErrorType.idNotExisits;
					
					else if (checkAccountValidity(userDestinationId, destinationAccount,
							sourceAccount) != ErrorType.checkOk)
						errorType = checkAccountValidity(userDestinationId, destinationAccount, sourceAccount);
					
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			}
		}


		String ctxpath = getServletContext().getContextPath();
		String path;
		

		if (errorType == ErrorType.checkOk) {
			// create transfer
			TransferDAO transferDAO = new TransferDAO(connection);
			try {
				transferDAO.createTransfer(sourceAccount, destinationAccount, amount);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Not possible to create the transfer - DB");
				return;
			}


			// -> SummaryPage
			session.setAttribute("source", sourceAccount);
			session.setAttribute("userDest", userDestinationId);
			session.setAttribute("destination", destinationAccount);
			path = ctxpath + "/GoToSummaryPage";

		} else {
			// -> ErrorPage
			session.setAttribute("bank_id", sourceAccount);
			session.setAttribute("errorType", errorType.getErrorType());
			path = ctxpath + "/GoToErrorPage";
		}

		response.sendRedirect(path);
	}

	// check if the balance is enough to allow the transfer
	public boolean checkBalanceValidity(int sourceAccount, double amount, HttpServletResponse response) throws SQLException {

		// get balance
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		double balanceFromDB;
		balanceFromDB = bankAccountDAO.getBalance(sourceAccount);
		return ((balanceFromDB >= amount));

	}

	// check if the destination bankId belong to the destination user
		public ErrorType checkAccountValidity(int userDestId, int destAccount, int sourceAccount)
				throws IOException, SQLException {

			// get user by bank account from DB
			BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
			User userFromDB;

			userFromDB = bankAccountDAO.getUserbyBankAccount(destAccount);

			//destination bank account doesn't exist
			if (userFromDB == null) return ErrorType.accountNotExists;
			
			//The destination user name doesn't match the bank ID you indicated
			if ((userFromDB.getId() != userDestId)) return ErrorType.userDestNotMatchDestAccount;
			
			
			if (sourceAccount == destAccount) return ErrorType.selfTransfer;		
			
			//check ok
			return ErrorType.checkOk;

		}
		
		//check if the user exists
		public boolean checkDestinationUserExist(int userDestId) throws SQLException {
			
			UserDAO userDAO = new UserDAO(connection);
			if (userDAO.getUserByID(userDestId) != null) return true; 
			else return false;
			
		}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
