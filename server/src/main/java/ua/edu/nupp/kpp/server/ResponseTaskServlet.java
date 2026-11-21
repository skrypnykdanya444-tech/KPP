package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ResponseTaskServlet extends HttpServlet {

    private final ResponseTaskService service = new ResponseTaskService();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<ResponseTask> list = service.getAll();
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
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String action = req.getParameter("action");

        try {
            if ("add".equals(action)) {
                ResponseTask t = new ResponseTask(
                        null,
                        Long.parseLong(req.getParameter("incidentId")),
                        req.getParameter("description"),
                        parseDate(req.getParameter("plannedDate"))
                );
                service.add(t);
                mapper.writeValue(resp.getWriter(), t);
            } else if ("update".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                ResponseTask t = new ResponseTask(
                        id,
                        Long.parseLong(req.getParameter("incidentId")),
                        req.getParameter("description"),
                        parseDate(req.getParameter("plannedDate"))
                );
                service.update(t);
                mapper.writeValue(resp.getWriter(), t);
            } else if ("delete".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                service.delete(id);
                mapper.writeValue(resp.getWriter(), "deleted");
            } else {
                writeError(resp, 400, "Unknown action: " + action);
            }
        } catch (NumberFormatException e) {
            writeError(resp, 400, "Invalid numeric parameter");
        } catch (DateTimeParseException e) {
            writeError(resp, 400, "Невірний формат дати. Очікується YYYY-MM-DDTHH:mm");
        } catch (IllegalArgumentException e) {
            writeError(resp, 400, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, 500, e.getMessage());
        } catch (RuntimeException e) {
            writeError(resp, 500, e.getMessage());
        }
    }

    private LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(raw.trim());
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), new ErrorResponse(message));
    }

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
