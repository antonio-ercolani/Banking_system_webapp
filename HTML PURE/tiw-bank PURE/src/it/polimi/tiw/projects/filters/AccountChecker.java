package it.polimi.tiw.projects.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AccountChecker implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("Eseguendo account checker");
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String path = req.getServletContext().getContextPath() + "/GoToHomePage" ;

		HttpSession s = req.getSession();
		if (s.getAttribute("bank_id") == null) {
			res.sendRedirect(path);
			return;
		}
		chain.doFilter(request, response);
	}

}
