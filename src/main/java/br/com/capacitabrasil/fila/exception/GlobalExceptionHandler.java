package br.com.capacitabrasil.fila.exception;

import br.com.capacitabrasil.fila.config.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de erros no padrao RFC 9457 (Problem Details).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), request);
        problem.setProperty("codigo", ex.getCodigo());
        return problem;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail handleRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Regra de negocio violada", ex.getMessage(), request);
        problem.setProperty("codigo", ex.getCodigo());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Erro de validacao",
                "Um ou mais campos estao invalidos.", request);
        problem.setProperty("codigo", "ERRO_VALIDACAO");
        problem.setProperty("erros", erros);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
                erros.put(v.getPropertyPath().toString(), v.getMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Erro de validacao",
                "Um ou mais parametros estao invalidos.", request);
        problem.setProperty("codigo", "ERRO_VALIDACAO");
        problem.setProperty("erros", erros);
        return problem;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ProblemDetail handleRequisicaoInvalida(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "Nao foi possivel interpretar a requisicao.", request);
        problem.setProperty("codigo", "REQUISICAO_INVALIDA");
        return problem;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.UNAUTHORIZED, "Nao autenticado",
                "Credenciais invalidas ou usuario inativo.", request);
        problem.setProperty("codigo", "CREDENCIAIS_INVALIDAS");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.FORBIDDEN, "Acesso negado",
                "Voce nao tem permissao para executar esta operacao.", request);
        problem.setProperty("codigo", "ACESSO_NEGADO");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Conflito de dados",
                "A operacao viola uma restricao de integridade dos dados.", request);
        problem.setProperty("codigo", "CONFLITO_DADOS");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
        log.error("Erro nao esperado", ex);
        ProblemDetail problem = problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
        problem.setProperty("codigo", "ERRO_INTERNO");
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", LocalDateTime.now(clock));
        problem.setProperty("traceId", MDC.get(TraceIdFilter.TRACE_ID));
        return problem;
    }
}
