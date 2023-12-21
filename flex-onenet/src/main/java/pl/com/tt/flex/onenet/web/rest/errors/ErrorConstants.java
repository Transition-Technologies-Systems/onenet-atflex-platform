package pl.com.tt.flex.onenet.web.rest.errors;

import java.net.URI;

public class ErrorConstants {

	public static final String ERR_OBJECT_MODIFIED_BY_ANOTHER_USER = "error.objectModifiedByAnotherUser";
	public static final String ERR_VALIDATION = "error.validation";
	public static final String PROBLEM_BASE_URL = "https://www.flex-platform/problem";
	public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
	public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
	public static final String UNEXPECTED_ERROR = "error.unexpectedError";

	//onenet user
	public static final String WRONG_LOGIN = "error.onenetUser.wrongLogin";
	public static final String WRONG_PASSWORD = "error.onenetUser.wrongPassword";
	public static final String USER_ALREADY_ADDED = "error.onenetUser.userAlreadyAdded";
	public static final String NO_ACTIVE_USER_FOUND = "error.onenetUser.noActiveUserFound";
	public static final String CANNOT_REMOVE_ACTIVE_USER = "error.onenetUser.cannotRemoveActiveUser";

	//api
	public static final String PROBLEM_CONNECTING_TO_ONENET = "error.api.unknownErrorConnectingToOnenetSystem";
	public static final String COULD_NOT_SEND_A_FILE = "error.api.couldNotSendAFile";

}
