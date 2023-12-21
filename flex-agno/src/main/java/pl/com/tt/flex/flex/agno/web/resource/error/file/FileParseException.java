package pl.com.tt.flex.flex.agno.web.resource.error.file;


import pl.com.tt.flex.flex.agno.web.resource.error.BadRequestAlertException;
import pl.com.tt.flex.flex.agno.web.resource.error.ErrorConstants;

/**
 * Common error for file parsing.
 */
public class FileParseException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public FileParseException() {
        super("Error occurred while parsing file!", "file", ErrorConstants.FILE_PARSE_ERROR);
    }
}
