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

		HttpSession s = req.getSession();
		if (s.getAttribute("bank_id") == null) {
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			return;
		}
		chain.doFilter(request, response);
	}

}
