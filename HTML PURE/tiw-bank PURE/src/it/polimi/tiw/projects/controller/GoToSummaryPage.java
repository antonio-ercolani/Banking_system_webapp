package it.polimi.tiw.projects.controller;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.UserDAO;
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

@WebServlet("/GoToSummaryPage")
public class GoToSummaryPage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;

    public GoToSummaryPage() {
        super();
    }

    public void init() throws ServletException {
    	
    	ServletContext servletContext = getServletContext();
		
		connection = ConnectionHandler.getConnection(servletContext);
		
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = "/WEB-INF/TransferSummaryPage.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        HttpSession session = request.getSession();

        float source_balance = 0;
        float destination_balance = 0;
        User userDest = null;

        int s_bankID = (int) session.getAttribute("source");
        int d_bankID = (int) session.getAttribute("destination");
        int userDestID = (int) session.getAttribute("userDest");

        BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
        UserDAO userDAO = new UserDAO(connection);
        try {
            source_balance = bankAccountDAO.getBalance(s_bankID);
            destination_balance = bankAccountDAO.getBalance(d_bankID);
            userDest = userDAO.getUserByID(userDestID);
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ctx.setVariable("source", s_bankID);
        ctx.setVariable("userDest", userDest);
        ctx.setVariable("source_balance", source_balance);
        ctx.setVariable("destination_balance", destination_balance);
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
