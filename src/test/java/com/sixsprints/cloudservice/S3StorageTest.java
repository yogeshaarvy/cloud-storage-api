package com.sixsprints.cloudservice;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.google.common.collect.Lists;
import com.sixsprints.cloudservice.dto.Credentials;
import com.sixsprints.cloudservice.dto.FileDto;
import com.sixsprints.cloudservice.service.CloudStorage;
import com.sixsprints.cloudservice.service.impl.S3CloudStorage;

public class S3StorageTest {

  private static final String ACCESS_ID = "";

  private static final String SECRET_KEY = "";

  private static final String BUCKET_NAME = "";

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

  public void testShouldProcessBatch() throws IOException {
    CloudStorage storageService = storage();
    storageService.downloadAndBatchProcess("out.csv", BUCKET_NAME, 100, this::process);
  }

  private CloudStorage storage() {
    CloudStorage storageService = new S3CloudStorage(
      Credentials.builder().accessId(ACCESS_ID).secretKey(SECRET_KEY).region(Regions.AP_SOUTH_1)
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
