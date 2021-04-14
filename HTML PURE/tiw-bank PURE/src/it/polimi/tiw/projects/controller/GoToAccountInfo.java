package it.polimi.tiw.projects.controller;

import it.polimi.tiw.projects.beans.Transfer;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
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
import java.util.ArrayList;
import java.util.List;


@WebServlet("/GoToAccountInfo")
public class GoToAccountInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;
	
    
    public GoToAccountInfo() {
        super();
    }
    
    public void init() throws ServletException {
    	
    	ServletContext context = getServletContext();
    	
    	//initialize database connection
		connection = ConnectionHandler.getConnection(context);
    	
    	//initialize template engine
    	ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
    	
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
				
		//get account balance
		HttpSession session = request.getSession();
		int bankAccountId = (Integer) session.getAttribute("bank_id");
		BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
		double balance;
		try {
			balance = bankAccountDAO.getBalance(bankAccountId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover account balance");
			return;
		}
		
		//get account transfers list
		TransferDAO transferDAO = new TransferDAO(connection);
		List<Transfer> transfers = new ArrayList<Transfer>();
		try {
			transfers = transferDAO.getAllbyBankAccount(bankAccountId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover account balance");
			return;
		}
		
		
		//--> AccountInfoPage
		String path = "/WEB-INF/AccountInfoPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("bankId",bankAccountId);
		ctx.setVariable("balance", balance);
		ctx.setVariable("transfers", transfers);
		templateEngine.process(path, ctx, response.getWriter());
		
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
