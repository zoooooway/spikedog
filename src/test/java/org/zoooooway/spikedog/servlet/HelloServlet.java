package org.zoooooway.spikedog.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zoooooway
 */
@WebServlet(name = "hello", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String html = String.format("<h1>Hello %s</h1>", req.getParameter("name"));
        try (PrintWriter writer = resp.getWriter()) {
            resp.setHeader("Content-Type", "text/html; charset=utf-8");
            resp.setHeader("Cache-Control", "no-cache");
            resp.setStatus(200);
            writer.write(html);
            resp.flushBuffer();
        }
    }
}
