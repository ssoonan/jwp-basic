package next.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import core.db.DataBase;
import next.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/user/update")
public class UpdateUserServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(CreateUserServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession();
        Object currentUserValue = session.getAttribute("user");

        if (currentUserValue == null) {
            resp.sendRedirect("/user/login.jsp");
            return;
        }

        User currentUser= (User)currentUserValue;
        User modifyUser = DataBase.findUserById(req.getParameter("userId"));

        if (currentUser != modifyUser) {
            resp.sendRedirect("/index.jsp");
            return;
        }

        log.debug("modify user: {}", modifyUser.getUserId());
        req.setAttribute("user", modifyUser);
        RequestDispatcher rd = req.getRequestDispatcher("/user/update.jsp");
        rd.forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = DataBase.findUserById(req.getParameter("userId"));
        user.setPassword(req.getParameter("password"));
        user.setName(req.getParameter("name"));
        user.setEmail(req.getParameter("email"));
        log.debug("modify user: {} is completed", user.getUserId());
        resp.sendRedirect("/user/list");

    }
}
