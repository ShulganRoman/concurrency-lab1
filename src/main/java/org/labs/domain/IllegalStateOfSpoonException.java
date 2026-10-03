package org.labs.domain;

public class IllegalStateOfSpoonException extends RuntimeException {
    public IllegalStateOfSpoonException() {
        super("Illegal state of spoon");
    }
}
