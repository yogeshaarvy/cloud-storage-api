package com.sixsprints.cloudservice.service.impl;

import java.io.IOException;
import java.nio.file.Path;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sixsprints.cloudservice.dto.Credentials;
import com.sixsprints.cloudservice.dto.FileDto;
import com.sixsprints.cloudservice.service.CloudStorage;

public class GoogleCloudStorage extends AbstractCloudStorageService implements CloudStorage {

  private static final String BASE_URL = "https://storage.googleapis.com/";

  final Storage storage;

  public GoogleCloudStorage(Credentials cred) {
    try {
      com.google.auth.Credentials credentials = GoogleCredentials.fromStream(cred.getFile());
      this.storage = StorageOptions.newBuilder().setCredentials(credentials)
        .setProjectId(cred.getProjectId()).build().getService();
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid Credentials Passed");
    }
  }

  @Override
  public String upload(final FileDto fileDto, final String bucket) {
    final String fileName = fileDto.getFileName();
    byte[] bytes = fileToBytes(fileDto);
    storage.create(
      BlobInfo.newBuilder(bucket, fileName).build(), bytes);
    return new StringBuffer(BASE_URL).append(bucket).append("/").append(fileDto.getFileName()).toString();
  }

  @Override
  public Path download(String key, String bucket, String dir) throws IOException {
    Path outputFile = createTempFile(key, dir);
    Blob blob = storage.get(BlobId.of(bucket, key));
    blob.downloadTo(outputFile);
    return outputFile;
  }

}