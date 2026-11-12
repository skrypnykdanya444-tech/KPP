package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet маршрутів.
 *
 * <p>Відповідає на GET (список) та POST (додавання) запити.</p>
 */
public class RouteServlet extends HttpServlet {

    private final RouteService service = new RouteService();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<Route> list = service.getAll();
            mapper.writeValue(resp.getWriter(), list);
        } catch (IllegalArgumentException e) {
            writeError(resp, 400, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, 500, e.getMessage());
        } catch (RuntimeException e) {
            writeError(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Tomcat за замовчуванням декодує тіло POST у ISO-8859-1.
        // Явно встановлюємо UTF-8, щоб українські літери передавалися коректно.
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String action = req.getParameter("action");

        try {
            if ("add".equals(action)) {
                Route r = new Route(
                        null,
                        Long.parseLong(req.getParameter("vehicleId")),
                        req.getParameter("routeNumber"),
                        req.getParameter("stopName"),
                        Double.parseDouble(req.getParameter("passengerFlow")),
                        Double.parseDouble(req.getParameter("routeImportance"))
                );
                service.add(r);
                mapper.writeValue(resp.getWriter(), r);
            } else if ("update".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                Route r = new Route(
                        id,
                        Long.parseLong(req.getParameter("vehicleId")),
                        req.getParameter("routeNumber"),
                        req.getParameter("stopName"),
                        Double.parseDouble(req.getParameter("passengerFlow")),
                        Double.parseDouble(req.getParameter("routeImportance"))
                );
                service.update(r);
                mapper.writeValue(resp.getWriter(), r);
            } else if ("delete".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                service.delete(id);
                mapper.writeValue(resp.getWriter(), "deleted");
            } else {
                writeError(resp, 400, "Unknown action: " + action);
            }
        } catch (NumberFormatException e) {
            writeError(resp, 400, "Invalid numeric parameter");
        } catch (IllegalArgumentException e) {
            writeError(resp, 400, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, 500, e.getMessage());
        } catch (RuntimeException e) {
            writeError(resp, 500, e.getMessage());
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), new ErrorResponse(message));
    }

    /** Тіло помилки у форматі JSON. */
    private static class ErrorResponse {
        private final String error;

        ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
