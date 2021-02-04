package com.mihey.restapitos3.controller;

import com.mihey.restapitos3.service.impl.S3ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class S3Controller {

    @Autowired
    private S3ServiceImpl service;

    @PostMapping
    public ResponseEntity<String> upload( @RequestParam("file") MultipartFile file) throws IOException {
        String message = service.upload(file);
        service.sendMessage(message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
