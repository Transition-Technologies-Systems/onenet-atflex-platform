package pl.com.tt.flex.flex.agno.algorithm.process.builder.kdm_mod;

public class KdmModNotAcquiredException extends Exception {
    public KdmModNotAcquiredException(String kdmFile) {
        super("Could not found kdm_mod file: " + kdmFile);
    }
}
