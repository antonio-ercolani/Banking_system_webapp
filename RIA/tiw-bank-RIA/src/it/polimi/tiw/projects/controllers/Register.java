package it.polimi.tiw.projects.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
@MultipartConfig
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
     

    public Register() {
        super();
    }
    
	public void init() throws ServletException {
		// initialize database connection
   		connection = ConnectionHandler.getConnection(getServletContext());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = null;
		String password = null;
		String repeat_password = null;
		String name = null;
		String surname = null;
		String email = null;

		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			repeat_password = StringEscapeUtils.escapeJava(request.getParameter("repeatpwd"));
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));

			if (username == null || password == null || repeat_password == null || name == null || surname == null || email == null)
				throw new NullPointerException();
			
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Bad request");
			return;
		}
			
		UserDAO uDAO = new UserDAO(connection);
		if (!uDAO.checkUniqueness(username)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Username not avaiable");
			return;
		} else {
			if (!password.contentEquals(repeat_password)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Mismatching between the fields password and repeat password");
				return;
			} else {
				if (!checkEmailValidity(email)) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Email address is incorrect");
					return;
				} else {
					try {
						uDAO.createUser(username, password, name, surname, email);
					} catch (SQLException | NoSuchAlgorithmException e) {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						response.getWriter().println("Internal Server error");
						e.printStackTrace();
						return;
					}
					response.setStatus(HttpServletResponse.SC_OK);
					response.getWriter().println("User registered correctly");
				}

			}
		}

	}

	private boolean checkEmailValidity(String email) {
		Pattern email_validation = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = email_validation.matcher(email);
		return matcher.find();
	}
		
		public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
