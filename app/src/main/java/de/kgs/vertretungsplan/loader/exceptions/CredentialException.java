package de.kgs.vertretungsplan.loader.exceptions;

/**
 * This exception gets thrown when the provided credentials are wrong
 */
public class CredentialException extends Exception {

    public CredentialException() {
        super();
    }

    public CredentialException(String message) {
        super(message);
    }
}
