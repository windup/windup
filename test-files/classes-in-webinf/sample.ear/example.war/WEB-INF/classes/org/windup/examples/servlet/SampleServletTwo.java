package org.windup.examples.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet({"/sampleservlettwo"})
public class SampleServletTwo extends HttpServlet {
   private static final long serialVersionUID = 1L;

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
   }
}
