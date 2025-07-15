package com.dialoguebranch.web.service.exception;

import com.dialoguebranch.exception.DialogueBranchException;

public class InvalidRoleException extends DialogueBranchException {


    /**
     * Creates an instance of a {@link DialogueBranchException} with a given {@code message},
     * explaining the cause of the exception.
     *
     * @param message the message explaining the cause of the exception.
     */
    public InvalidRoleException(String message) {
        super(message);
    }
}
