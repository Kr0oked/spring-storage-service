package de.bobek.spring.storageservice.storage.internal;

import java.io.IOException;
import java.io.InputStream;

public interface ContentStore {

    InputStream get(String id) throws IOException;

    void store(String id, InputStream content) throws IOException;

    void delete(String id) throws IOException;
}
