package com.tutorhub.common.storage;

import com.tutorhub.common.config.AppProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
class MinioFileStorage implements FileStorage {
    private final MinioClient client; private final String bucket;
    MinioFileStorage(AppProperties properties) { var storage = properties.getStorage(); client = MinioClient.builder().endpoint(storage.getEndpoint()).credentials(storage.getAccessKey(), storage.getSecretKey()).build(); bucket = storage.getBucket(); }
    @Override public String store(String key, InputStream content, long size, String contentType) { try { client.putObject(PutObjectArgs.builder().bucket(bucket).object(key).stream(content, size, -1).contentType(contentType).build()); return key; } catch (Exception e) { throw new IllegalStateException("Could not store file", e); } }
    @Override public void delete(String key) { try { client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build()); } catch (Exception e) { throw new IllegalStateException("Could not delete file", e); } }
    @Override public String presignedGet(String key) { try { return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucket).object(key).method(Method.GET).expiry(15, TimeUnit.MINUTES).build()); } catch (Exception e) { throw new IllegalStateException("Could not sign file URL", e); } }
}
