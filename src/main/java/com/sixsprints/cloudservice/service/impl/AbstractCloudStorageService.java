package com.sixsprints.cloudservice.service.impl;

import static java.util.UUID.randomUUID;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.imageio.ImageIO;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.sixsprints.cloudservice.dto.FileDto;
import com.sixsprints.cloudservice.service.CloudStorage;

public abstract class AbstractCloudStorageService implements CloudStorage {

  @Override
  public String resizeAndUpload(final FileDto fileDto, final String bucket, final Double maxImageSize) {
    BufferedImage bufferedImage = fileToBufferedImage(fileDto);
    bufferedImage = resizeImage(bufferedImage, maxImageSize);
    File resizedFile = fileDtoToFile(fileDto);
    writeBufferedToFile(bufferedImage, resizedFile);
    return upload(cloneFileDto(fileDto, resizedFile), bucket);
  }

  @Override
  public Path download(String key, String bucket) throws IOException {
    Path tmp = Files.createTempDirectory(null, new FileAttribute<?>[0]);
    return download(key, bucket, tmp.toAbsolutePath().toString());
  }

  @Override
  public <T> List<T> downloadAndBatchProcess(String key, String bucket, int batchSize,
    Function<List<String>, List<T>> func) throws IOException {
    BiFunction<List<String>, Map<String, Object>, List<T>> biFunc = new BiFunction<List<String>, Map<String, Object>, List<T>>() {
      @Override
      public List<T> apply(List<String> t, Map<String, Object> u) {
        return func.apply(t);
      }
    };
    return downloadAndBatchProcess(key, bucket, batchSize, biFunc, Maps.newHashMap());
  }

  @Override
  public <T> List<T> downloadAndBatchProcess(String key, String bucket, int batchSize,
    BiFunction<List<String>, Map<String, Object>, List<T>> func, Map<String, Object> extraProps)
    throws IOException {
    Path path = download(key, bucket);
    InputStream inputStream = Files.newInputStream(path);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    boolean moreLines = true;
    List<T> result = Lists.newArrayList();
    while (moreLines) {
      List<String> batch = readBatch(reader, batchSize);
      result.addAll(func.apply(batch, extraProps));
      if (batch.size() < batchSize) {
        moreLines = false;
      }
    }
    reader.close();
    inputStream.close();
    return result;
  }

  protected FileDto cloneFileDto(FileDto fileDto, File resizedFile) {
    return FileDto.builder().fileName(fileDto.getFileName()).fileToUpload(resizedFile).build();
  }

  protected void writeBufferedToFile(BufferedImage bufferedImage, File resizedFile) {
    try {
      ImageIO.write(bufferedImage, "jpg", resizedFile);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to re-write buffered image back to file");
    }
  }

  protected BufferedImage fileToBufferedImage(FileDto fileDto) {
    try {
      if (fileDto.getBytes() != null && fileDto.getBytes().length > 0) {
        return ImageIO.read(new ByteArrayInputStream(fileDto.getBytes()));
      }
      return ImageIO.read(fileDto.getFileToUpload());
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to convert file to buffered image");
    }

  }

  protected byte[] fileToBytes(FileDto dto) {
    if (dto.getBytes() != null && dto.getBytes().length > 0) {
      return dto.getBytes();
    }
    final File file = dto.getFileToUpload();
    try {
      return Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid file passed to upload");
    }
  }

  protected static BufferedImage resizeImage(BufferedImage originalImage, Double maxImageSize) {

    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

    Double width = Double.valueOf(originalImage.getWidth());
    Double height = Double.valueOf(originalImage.getHeight());
    if (width < maxImageSize && height < maxImageSize) {
      return originalImage;
    }
    if (width > height) {
      height = maxImageSize * (height / width);
      width = maxImageSize;
    } else {
      width = maxImageSize * (width / height);
      height = maxImageSize;
    }

    BufferedImage resizedImage = new BufferedImage(width.intValue(), height.intValue(), type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, width.intValue(), height.intValue(), null);
    g.dispose();
    return resizedImage;
  }

  protected Path createTempFile(String key, String dir) throws IOException {
    Path path = Paths.get(dir, randomUUID().toString());
    Files.createDirectories(path);
    return Paths.get(path.toAbsolutePath().toString(), key.replaceAll("/", "-"));
  }

  private List<String> readBatch(BufferedReader reader, int batchSize) throws IOException {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < batchSize; i++) {
      String line = reader.readLine();
      if (line != null) {
        result.add(line);
      } else {
        return result;
      }
    }
    return result;
  }

  private File fileDtoToFile(FileDto fileDto) {
    if (fileDto.getFileToUpload() != null) {
      return fileDto.getFileToUpload();
    }
    try {
      return Files
        .write(createTempFile(randomUUID().toString() + fileDto.getFileName(),
          Files.createTempDirectory(null, new FileAttribute<?>[0]).toAbsolutePath().toString()), fileDto.getBytes())
        .toFile();
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }

  }

}
