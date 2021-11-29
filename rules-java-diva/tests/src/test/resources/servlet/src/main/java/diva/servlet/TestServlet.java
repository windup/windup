package diva.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet(name = "TradeAppServlet", urlPatterns = { "/app" })
public class TestServlet extends HttpServlet {

    static Context context;
    static DataSource dataSource;

    static ObjectMapper JSON_SERIALIZER = new ObjectMapper();

    static {
        try {
            context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/DivaDataSource)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn;
        try {
            conn = dataSource.getConnection();
            String id = request.getParameter("id");
            PreparedStatement stmt = conn.prepareStatement("select * from TEST where ID = " + id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().println(JSON_SERIALIZER.writeValueAsString(new LinkedHashMap() {
                    {
                        put("id", rs.getString("ID"));
                        put("value", rs.getString("VALUE"));
                    }
                }));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

    }

}
