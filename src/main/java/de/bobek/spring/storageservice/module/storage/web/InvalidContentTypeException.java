package de.bobek.spring.storageservice.module.storage.web;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidContentTypeException extends RuntimeException {

    private static final long serialVersionUID = 2351911082654624639L;

    public InvalidContentTypeException(Throwable cause) {
        super("Invalid Content-Type header", cause);
    }
}
