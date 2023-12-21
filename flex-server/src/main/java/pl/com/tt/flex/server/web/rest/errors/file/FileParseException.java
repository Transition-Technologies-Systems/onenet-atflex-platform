package pl.com.tt.flex.server.web.rest.errors.file;

import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;

/**
 * Common error for file parsing.
 */
public class FileParseException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public FileParseException() {
        super("Error occurred while parsing file!", "file", ErrorConstants.FILE_PARSE_ERROR);
    }
}
