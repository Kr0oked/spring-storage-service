package de.bobek.spring.storageservice.module.storage.internal;

public class ContentException extends RuntimeException {

    private static final long serialVersionUID = 5990261674552565586L;

    public ContentException(String contentId, Throwable cause) {
        super(String.format("Operation for content '%s' failed", contentId), cause);
    }
}
