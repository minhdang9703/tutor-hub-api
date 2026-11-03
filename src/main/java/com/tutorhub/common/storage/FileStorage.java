package com.tutorhub.common.storage;
import java.io.InputStream;
public interface FileStorage {
    String store(String key, InputStream content, long size, String contentType);
    void delete(String key);
    String presignedGet(String key);
}
