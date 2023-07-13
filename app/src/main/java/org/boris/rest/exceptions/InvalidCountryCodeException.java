package org.boris.rest.exceptions;

public class InvalidCountryCodeException extends Exception{

    public InvalidCountryCodeException(String message) {
        super(message);
    }
}
