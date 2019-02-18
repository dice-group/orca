package org.dice_research.ldcbench.nodes.http.simple;

import org.simpleframework.http.Status;

public class SimpleHttpException extends Exception {

    private static final long serialVersionUID = 1L;
    
    protected Status status;

    public SimpleHttpException(String message, Throwable cause, Status status) {
        super(message, cause);
        this.status = status;
    }

    public SimpleHttpException(String message, Status status) {
        super(message);
        this.status = status;
    }
    
    public Status getStatus() {
        return status;
    }
}
