package org.dice_research.ldcbench.nodes.http.simple;

import org.simpleframework.http.Status;

public class SimpleHTTPException extends Exception {

    private static final long serialVersionUID = 1L;
    
    protected Status status;

    public SimpleHTTPException(String message, Throwable cause, Status status) {
        super(message, cause);
        this.status = status;
    }

    public SimpleHTTPException(String message, Status status) {
        super(message);
        this.status = status;
    }
    
    public Status getStatus() {
        return status;
    }
}
