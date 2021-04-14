package it.polimi.tiw.projects.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.AddressBookDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;

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

import static java.lang.Integer.parseInt;

@WebServlet("/GetAddressBook")
public class GetAddressBook extends HttpServlet {

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
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("user");

        try {
            List<User> addressBook = addressBookDAO.getAllByUser(user.getId());

            List<String> ids = new ArrayList<>();
            for(User u : addressBook) {
                ids.add(String.valueOf(u.getId()));
            }
            String json = new Gson().toJson(ids);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (SQLException e) {
            e.printStackTrace();
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