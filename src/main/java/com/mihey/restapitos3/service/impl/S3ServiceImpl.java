package com.mihey.restapitos3.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.gson.Gson;
import com.mihey.restapitos3.model.Notification;
import com.mihey.restapitos3.model.Status;
import com.mihey.restapitos3.model.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class S3ServiceImpl {

    @Value("${cloud.aws.end-point.url}")
    private String destinationName;
    @Value("${destinationDirectory}")
    private String destinationDirectory;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.url}")
    private String url;
    private Gson gson;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    private final AmazonS3 s3client;

    @Autowired
    public S3ServiceImpl(AmazonS3 s3client) {
        this.s3client = s3client;
        this.gson = new Gson();
    }

    public String upload(MultipartFile zipFile) {

        if (zipFile.isEmpty()) {
            return gson.toJson(new Notification("File is empty!", Status.NEW, Type.ERROR));
        }
        String zipFileName = null;
        try {
            zipFileName = unZip(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return gson.toJson(new Notification(e.getMessage(), Status.NEW, Type.ERROR));
        }
        MultipleFileUpload upload = null;
        try {
            TransferManager transferManager = TransferManagerBuilder.standard()
                    .withS3Client(s3client).build();
            File uploadFile = new File(destinationDirectory + "/" + zipFileName);
            upload = transferManager
                    .uploadDirectory(bucket, zipFileName, uploadFile, true);
        } catch (Exception e) {
            return gson.toJson(new Notification(e.getMessage(), Status.NEW, Type.ERROR));
        }
        try {
            upload.waitForCompletion();
        } catch (InterruptedException e) {
            return gson.toJson(new Notification(e.getMessage(), Status.NEW, Type.ERROR));
        }
        if (upload.isDone()) {
            return gson.toJson(new Notification("file location: " + url + bucket + "/" + zipFileName,
                    Status.NEW, Type.INFO));
        }
        return gson.toJson(new Notification("File didn't upload! Try again!", Status.NEW, Type.ERROR));
    }

    public void sendMessage(String message) {
        try {
            queueMessagingTemplate.convertAndSend(destinationName,message, Map.of("trace-id", UUID.randomUUID().toString()));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String unZip(MultipartFile file) throws IOException {
        File destDir = new File(destinationDirectory);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(convertMultiPartToFile(file)));
        ZipEntry zipEntry = zis.getNextEntry();
        String name = null;
        if (zipEntry != null) {
            name = zipEntry.getName();
        }
        if (name == null) {
            throw new IOException("Wrong file format!");
        }

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory" + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory" + parent);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        return name;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {

        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        if (!isValid(convFile)) {
            throw new IOException("This is not zip archive!");
        }
        return convFile;
    }

    // Validation zip archive by file signature
    private boolean isValid(File file) throws IOException {
        final int ZIP_FILE_SIGNATURE = 0x504B0304;
        int firstByte = 0;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        firstByte = randomAccessFile.readInt();
        if (firstByte == 0x504B0506 || firstByte == 0x504B0708) {  // 0x504B0506 - archive is empty, 0x504B0708 - archive is spanned
            throw new IOException("Archive is empty or spanned!");
        }
        return firstByte == ZIP_FILE_SIGNATURE;
    }
}

