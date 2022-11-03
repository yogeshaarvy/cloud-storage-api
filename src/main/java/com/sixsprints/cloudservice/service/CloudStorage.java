package com.sixsprints.cloudservice.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.sixsprints.cloudservice.dto.FileDto;

public interface CloudStorage {

  String upload(FileDto fileDto, String bucket);

  String resizeAndUpload(FileDto fileDto, String bucket, Double maxImageSize);

  Path download(String key, String bucket) throws IOException;

  Path download(String key, String bucket, String dir) throws IOException;

  <T> List<T> downloadAndBatchProcess(String key, String bucket, int batchSize,
    Function<List<String>, List<T>> func)
    throws IOException;

  <T> List<T> downloadAndBatchProcess(String key, String bucket, int batchSize,
    BiFunction<List<String>, Map<String, Object>, List<T>> func, Map<String, Object> extraProps)
    throws IOException;

}
