package com.sixsprints.cloudservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.sixsprints.cloudservice.dto.Credentials;
import com.sixsprints.cloudservice.dto.FileDto;
import com.sixsprints.cloudservice.service.CloudStorage;
import com.sixsprints.cloudservice.service.impl.GoogleCloudStorage;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GoogleCloudStorageTest extends TestCase {

  public GoogleCloudStorageTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(GoogleCloudStorageTest.class);
  }

  private static final String PROJECT_ID = "";

  private static final String BUCKET_NAME = "";

  private static final String AUTH_JSON = "";

  public void testShouldUpload() throws IOException {
    CloudStorage storageService = storage();
    String upload = storageService.upload(createFileDto(0), BUCKET_NAME);
    System.out.println(upload);
  }

  public void testShouldResizeAndUpload() throws IOException {
    CloudStorage storageService = storage();
    String upload = storageService.resizeAndUpload(createFileDto(1), BUCKET_NAME, 50D);
    System.out.println(upload);
  }

  public void testShouldDownload() throws IOException {
    CloudStorage storageService = storage();
    Path path = storageService.download("0flower.jpeg", BUCKET_NAME);
    System.out.println(path);
    Files.delete(path);
  }

  public void testShouldProcessBatch() throws IOException {
    CloudStorage storageService = storage();
    storageService.downloadAndBatchProcess("out.csv", BUCKET_NAME, 100, this::process);
  }

  private CloudStorage storage() throws IOException {
    InputStream stream = Resources.getResource(AUTH_JSON).openStream();
    CloudStorage storageService = new GoogleCloudStorage(
      Credentials.builder().file(stream).projectId(PROJECT_ID)
        .build());
    return storageService;
  }

  private FileDto createFileDto(int i) {
    return FileDto.builder().fileName(i + "flower.jpeg")
      .fileToUpload(new File("/Users/karan/Desktop/Misc/Pics/download.jpeg"))
      .build();
  }

  private List<String> process(List<String> batch) {
    return Lists.newArrayList();
  }

}
