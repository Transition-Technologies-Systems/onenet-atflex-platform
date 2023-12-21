package pl.com.tt.flex.flex.agno.web.resource.error;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_OBJECT_MODIFIED_BY_ANOTHER_USER = "error.objectModifiedByAnotherUser";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.flex-platform/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final String UNEXPECTED_ERROR = "error.unexpectedError";


    public static final String FILE_PARSE_ERROR = "error.fileParseError";


    public static final String CANNOT_DELETE_BECAUSE_KDM_MODEL_IS_USED_IN_ALGORITHM_EVALUATION =
          "error.kdmModel.cannotDeleteBecauseKdmModelIsUsedInAlgorithmEvaluation";
    public static final String CANNOT_CREATE_BECAUSE_KDM_MODEL_WITH_THIS_AREA_NAME_ALREADY_EXIST =
            "error.kdmModel.cannotCreateBecauseKdmModelWithThisAreaNameIsAlreadyExist";
    public static final String CANNOT_UPDATE_BECAUSE_KDM_MODEL_WITH_THIS_AREA_NAME_ALREADY_EXIST =
            "error.kdmModel.cannotUpdateBecauseKdmModelWithThisAreaNameIsAlreadyExist";
    public static final String CANNOT_ADD_KDM_MODEL_TIMESTAMP_FILE_BECAUSE_WRONG_EXTENSION =
            "error.kdmModel.cannotAddKdmModelTimestampFileBecauseWrongExtension";
    public static final String WRONG_KDM_MODEL_TIMESTAMP_NUMBER = "error.kdmModel.wrongKdmModelTimestampNumber";
    public static final String KDM_MODEL_DOES_NOT_EXIST = "error.kdmModel.kdmModelDoesNotExist";
    public static final String NODES_LABEL_NOT_FOUND = "error.kdmModel.nodesLabelNotFound";
    public static final String NODES_CONTENT_NOT_FOUND = "error.kdmModel.nodesContentNotFound";
    public static final String MISSING_COMMAS = "error.kdmModel.missingCommas";
    public static final String MISSING_DOTS = "error.kdmModel.missingDots";
    public static final String BRANCHES_LABEL_NOT_FOUND = "error.kdmModel.branchesLabelNotFound";
    public static final String BRANCHES_CONTENT_NOT_FOUND = "error.kdmModel.branchesContentNotFound";
    public static final String GEN_LABELS_NOT_FOUND = "error.kdmModel.genLabelsNotFound";
    public static final String GEN_MISSING_LABEL = "error.kdmModel.genMissingLabel";
    public static final String GEN_CONTENT_NOT_FOUND = "error.kdmModel.genContentNotFound";
    public static final String GEN_MISSING_COLUMNS = "error.kdmModel.genMissingColumns";
    public static final String RECEPTION_LABELS_NOT_FOUND = "error.kdmModel.receptionLabelsNotFound";
    public static final String RECEPTION_MISSING_LABEL = "error.kdmModel.receptionMissingLabel";
    public static final String RECEPTION_CONTENT_NOT_FOUND = "error.kdmModel.receptionContentNotFound";
    public static final String RECEPTION_MISSING_COLUMNS = "error.kdmModel.receptionMissingColumns";

    private ErrorConstants() {
    }
}
