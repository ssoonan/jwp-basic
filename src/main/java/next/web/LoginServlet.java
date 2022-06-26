package next.web;


import core.db.DataBase;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/user/login")
public class LoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CreateUserServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = DataBase.findUserById(req.getParameter("userId"));

        if (user == null || !user.getPassword().equals(req.getParameter("password"))) {
            resp.sendRedirect("/user/login_failed.jsp");
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("user", user);
        log.debug("user {} is logined", user.getUserId());
        resp.sendRedirect("/index.jsp");

    }
}