package in.ignishers.eduportal.security;

import in.ignishers.eduportal.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        403,
                        "Forbidden",
                        "You do not have permission to access this resource"
                );

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}