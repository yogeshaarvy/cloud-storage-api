package com.sixsprints.cloudservice.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.sixsprints.cloudservice.dto.Credentials;
import com.sixsprints.cloudservice.dto.FileDto;
import com.sixsprints.cloudservice.service.CloudStorage;

public class S3CloudStorage extends AbstractCloudStorageService implements CloudStorage {

  private final AmazonS3 client;

  private Credentials cred;

  // "https://{bucket}.s3.{region}.amazonaws.com/{fileName}";
  private static final String BASE_URL = "https://%1$s.s3.%2$s.amazonaws.com/%3$s";

  public S3CloudStorage(Credentials cred) {
    this.cred = cred;
    AWSCredentials credentials = new BasicAWSCredentials(cred.getAccessId(), cred.getSecretKey());
    client = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .withRegion(cred.getRegion())
      .build();
  }

  @Override
  public String upload(FileDto fileDto, String bucket) {
    client.putObject(
      bucket,
      fileDto.getFileName(),
      fileDto.getFileToUpload());
    return String.format(BASE_URL, bucket, cred.getRegion().getName().toLowerCase(), fileDto.getFileName());
  }

  @Override
  public Path download(String key, String bucket, String dir) throws IOException {
    Path outputFile = createTempFile(key, dir);
    S3Object s3object = client.getObject(bucket, key);
    S3ObjectInputStream inputStream = s3object.getObjectContent();
    Files.copy(inputStream, outputFile);
    return outputFile;
  }

}
