package pl.com.tt.flex.server.web.rest.errors;

import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.web.util.HeaderUtil;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.*;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;
import pl.com.tt.flex.server.common.errors.ConcurrencyFailureException;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.common.errors.mail.EmailAlreadyUsedException;
import pl.com.tt.flex.server.common.errors.user.AccountResourceException;
import pl.com.tt.flex.server.common.errors.user.InvalidPasswordException;
import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.service.activityMonitor.ActivityMonitorService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.web.rest.errors.user.UserNotActivatedException;
import pl.com.tt.flex.server.web.rest.errors.user.UsernameNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.config.Constants.FLEX_APP_NAME_HEADER;
import static pl.com.tt.flex.server.util.StringUtils.createErrorMessage;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final Environment env;
    private final ActivityMonitorService activityMonitorService;
    private final UserService userService;

    public ExceptionTranslator(Environment env, ActivityMonitorService activityMonitorService, UserService userService) {
        this.env = env;
        this.activityMonitorService = activityMonitorService;
        this.userService = userService;
    }

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            }
        }

        // pominiecie zapisu komunikatow dla bledow formularza
        if (!ErrorConstants.CONSTRAINT_VIOLATION_TYPE.equals(problem.getType())) {
            saveActivityMonitor(request, problem, problem.getDetail(), ErrorConstants.UNEXPECTED_ERROR, ActivityEvent.UNEXPECTED_ERROR, null);
        }
        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName().replaceFirst("DTO$", ""), f.getField(), createMAVErrorMessage(f)))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    /**
     * Tworzenie sciezki bledow formularza. Przyklad dla @UniqueUserEmail: error.user.uniqueUserEmail
     */
    private String createMAVErrorMessage(FieldError f) {
        StringBuilder sb = new StringBuilder();
        sb.append("error.");
        sb.append(f.getObjectName().replaceFirst("DTO$", ""));
        sb.append(".");
        sb.append(WordUtils.uncapitalize(f.getCode()));
        return sb.toString();
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        saveActivityMonitor(request, ex, ex.getMessage(), ErrorConstants.UNEXPECTED_ERROR, ActivityEvent.UNEXPECTED_ERROR, null);
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, true, ex.getEntityName(), ex.getErrorKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleObjectValidationException(ObjectValidationException ex, NativeWebRequest request) {
        BadRequestAlertException problem = new BadRequestAlertException(ex.getMessage(), ex.getEntityName(), ex.getMsgKey());
        saveActivityMonitor(request, problem, ex.getMessage(), ex.getMsgKey(), ex.getActivityEvent(), ex.getObjectId());
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName, true, ex.getEntityName(), ex.getMsgKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ErrorConstants.ERR_OBJECT_MODIFIED_BY_ANOTHER_USER)
            .build();
        saveActivityMonitor(request, problem, ex.getMessage(), ex.getMsgKey(), ex.getActivityEvent(), ex.getObjectId());
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleEmailAlreadyUsedException(EmailAlreadyUsedException ex, NativeWebRequest request) {
        EmailAlreadyUsedException problem = new EmailAlreadyUsedException();
        saveActivityMonitor(request, problem, ex.getMessage(), ex.getErrorKey(), ex.getActivityEvent(), null);
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName, false, problem.getEntityName(), problem.getErrorKey(), problem.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleInvalidPasswordException(InvalidPasswordException ex, NativeWebRequest request) {
        return create(new InvalidPasswordException(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleUserNotActivatedException(UserNotActivatedException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, true, null, ex.getMsgKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleUsernameNotFoundException(UsernameNotFoundException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, true, null, ex.getMsgKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleAccountResourceException(AccountResourceException ex, NativeWebRequest request) {
        AccountResourceException problem = new AccountResourceException(ex.getMessage());
        return create(problem, request);
    }

    @Override
    public ProblemBuilder prepare(final Throwable throwable, final StatusType status, final URI type) {

        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            if (throwable instanceof HttpMessageConversionException) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.getReasonPhrase())
                    .withStatus(status)
                    .withDetail("Unable to convert http message")
                    .withCause(Optional.ofNullable(throwable.getCause())
                        .filter(cause -> isCausalChainsEnabled())
                        .map(this::toProblem)
                        .orElse(null));
            }
            if (throwable instanceof DataAccessException) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.getReasonPhrase())
                    .withStatus(status)
                    .withDetail("Failure during data access")
                    .withCause(Optional.ofNullable(throwable.getCause())
                        .filter(cause -> isCausalChainsEnabled())
                        .map(this::toProblem)
                        .orElse(null));
            }
            if (containsPackageName(throwable.getMessage())) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.getReasonPhrase())
                    .withStatus(status)
                    .withDetail("Unexpected runtime exception")
                    .withCause(Optional.ofNullable(throwable.getCause())
                        .filter(cause -> isCausalChainsEnabled())
                        .map(this::toProblem)
                        .orElse(null));
            }
        }

        return Problem.builder()
            .withType(type)
            .withTitle(status.getReasonPhrase())
            .withStatus(status)
            .withDetail(throwable.getMessage())
            .withCause(Optional.ofNullable(throwable.getCause())
                .filter(cause -> isCausalChainsEnabled())
                .map(this::toProblem)
                .orElse(null));
    }

    private boolean containsPackageName(String message) {

        // This list is for sure not complete
        return StringUtils.containsAny(message, "org.", "java.", "net.", "javax.", "com.", "io.", "de.", "pl.com.tt.flex.server");
    }

    private void saveActivityMonitor(NativeWebRequest request, Problem problem, String errorMessage, String errorKey, ActivityEvent activityEvent, Long objectId) {
        if (userService.isUserLoggedIn() && !Objects.isNull(activityEvent)) {
            HttpServletRequest httpServletRequest = request.getNativeRequest(HttpServletRequest.class);
            String requestUriPath = null;
            if (nonNull(httpServletRequest)) {
                requestUriPath = httpServletRequest.getMethod() + " - " + httpServletRequest.getRequestURI();
            }
            AppModuleName appName = nonNull(request.getHeader(FLEX_APP_NAME_HEADER)) ? AppModuleName.fromStringName(request.getHeader(FLEX_APP_NAME_HEADER)) : null;
            String httpResponseStatus = null;
            if (nonNull(problem.getStatus())) {
                httpResponseStatus = problem.getStatus().getStatusCode() + " - " + problem.getStatus().getReasonPhrase();
            }
            if (problem instanceof Throwable) {
                Throwable cause = (Throwable) problem;
                errorMessage = createErrorMessage(cause);
            }
            activityMonitorService.saveErrorEvent(errorMessage, errorKey, activityEvent, String.valueOf(objectId), appName, requestUriPath, httpResponseStatus);
        }
    }
}
