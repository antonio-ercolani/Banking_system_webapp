package it.polimi.tiw.projects.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.projects.beans.User;
import it.polimi.tiw.projects.dao.BankAccountDAO;
import it.polimi.tiw.projects.dao.TransferDAO;
import it.polimi.tiw.projects.dao.UserDAO;
import it.polimi.tiw.projects.utils.ConnectionHandler;
import it.polimi.tiw.projects.utils.ErrorType;
import org.json.JSONObject;

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

@WebServlet("/CreateTransfer")
@MultipartConfig
public class CreateTransfer extends HttpServlet {

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

        boolean badRequest = false;
        int sourceAccount = 0;
        int userDestinationId = 0;
        int destinationAccount = 0;
        double amount = 0;
        String description = null;
        HttpSession session = request.getSession();
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
            //response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ErrorType errorType = ErrorType.checkOk;
        //ERROR LEGEND --> SEE TransferErrorPage.html

        if (amount < 0) {
            errorType = ErrorType.negativeAmount;
        } else {
            try {
                if (!checkBalanceValidity(sourceAccount, amount, response))
                    errorType = ErrorType.insufficientBalance;
                else if (!checkDestinationUserExist(userDestinationId))
                    errorType = ErrorType.idNotExists;
                else if (checkAccountValidity(userDestinationId, destinationAccount, sourceAccount) != ErrorType.checkOk)
                    errorType = checkAccountValidity(userDestinationId, destinationAccount, sourceAccount);
            } catch (SQLException e) {
                //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                //		"Not possible to recover data from DB");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }

        if (errorType == ErrorType.checkOk) {
            // create transfer
            TransferDAO transferDAO = new TransferDAO(connection);
            try {
                transferDAO.createTransfer(sourceAccount, destinationAccount, amount);
            } catch (SQLException e) {
                //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                //		"Not possible to create the transfer - DB");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                e.printStackTrace();
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);


            // -> SummaryPage

            UserDAO userDAO = new UserDAO(connection);
            int id = ((User)session.getAttribute("user")).getId();
            User sender = null;
            User receiver = null;
            try {
                sender = userDAO.getUserByID(id);
                receiver = userDAO.getUserByID(userDestinationId);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
            float sourceBank = 0;
            float destBank = 0;
            try {
                sourceBank = bankAccountDAO.getBalance(sourceAccount);
                destBank = bankAccountDAO.getBalance(destinationAccount);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JSONObject jsn = new JSONObject();
            JSONObject senderJSON = new JSONObject();
            JSONObject receiverJSON = new JSONObject();

            senderJSON.put("name", sender.getName() );
            senderJSON.put("surname", sender.getSurname());
            senderJSON.put("balance", sourceBank);

            receiverJSON.put("name", receiver.getName() );
            receiverJSON.put("surname", receiver.getSurname());
            receiverJSON.put("balance", destBank);

            jsn.put("userID", senderJSON);
            jsn.put("destID", receiverJSON);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsn.toString());

        } else {

            // -> Error

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String json = new Gson().toJson(errorType.getErrorType());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }

    }

    /**
     *  check if the balance is enough to allow the transfer
     */
    public boolean checkBalanceValidity(int sourceAccount, double amount, HttpServletResponse response) throws SQLException {

        // get balance
        BankAccountDAO bankAccountDAO = new BankAccountDAO(connection);
        double balanceFromDB;
        balanceFromDB = bankAccountDAO.getBalance(sourceAccount);
        return ((balanceFromDB >= amount));

    }


    /**
     * check if the destination bankId belong to the destination user
     */
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

    /**
     * check if the user exists
     */
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
