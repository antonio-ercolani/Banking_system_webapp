package it.polimi.tiw.projects.controllers;

import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AddressBookDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.Integer.parseInt;

@WebServlet("/CheckContact")
@MultipartConfig
public class CheckContact extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
    	// initialize database connection
   		connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        AddressBookDAO addressBookDAO = new AddressBookDAO(connection);
        int contactID = 0;
        try {
            contactID = parseInt(request.getParameter("contact"));
        } catch (NumberFormatException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("user");
        try {
            if(!addressBookDAO.checkContact(user.getId(),contactID))
                response.setStatus(HttpServletResponse.SC_OK);
            else
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        }

    }
    
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
