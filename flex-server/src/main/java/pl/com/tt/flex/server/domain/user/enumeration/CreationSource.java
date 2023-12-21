package pl.com.tt.flex.server.domain.user.enumeration;

public enum CreationSource {
    SYSTEM, // generated during system initialization
    ADMIN, // created by administrator
    REGISTRATION // created by user in registration process
}
