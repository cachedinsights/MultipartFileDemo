package com.stackfortech.multipartDemo.controller;

import com.stackfortech.multipartDemo.model.UploadedFile;
import com.stackfortech.multipartDemo.response.FileUploadResponse;
import com.stackfortech.multipartDemo.service.FileUploadService;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload/local")
    public void uploadLocal(@RequestParam("file")MultipartFile multipartFile) throws IOException, ZipException {

            unzipFiles(multipartFile);
       //     fileUploadService.uploadToLocal(multipartFile);


    }
    @PostMapping("/upload/db")
    public FileUploadResponse uploadDb(@RequestParam("file")MultipartFile multipartFile)
    {
       UploadedFile uploadedFile = fileUploadService.uploadToDb(multipartFile);
       FileUploadResponse response = new FileUploadResponse();
       if(uploadedFile!=null){
           String downloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                   .path("/api/v1/download/")
                   .path(uploadedFile.getFileId())
                   .toUriString();
           response.setDownloadUri(downloadUri);
           response.setFileId(uploadedFile.getFileId());
           response.setFileType(uploadedFile.getFileType());
           response.setUploadStatus(true);
           response.setMessage("File Uploaded Successfully!");
           return response;

       }
       response.setMessage("Oops 1 something went wrong please re-upload.");
       return response;
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id)
    {
        UploadedFile uploadedFileToRet =  fileUploadService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(uploadedFileToRet.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename= "+uploadedFileToRet.getFileName())
                .body(new ByteArrayResource(uploadedFileToRet.getFileData()));
    }


    public void unzipFiles(MultipartFile file) throws IOException, ZipException {
        File zip  = File.createTempFile(UUID.randomUUID().toString(),"temp");
        FileOutputStream outputStream = new FileOutputStream(zip);
        IOUtils.copy(file.getInputStream(),outputStream);
        outputStream.close();

        String destination = "/Users/deomrinal/desktop/";
        ZipFile zipFile = new ZipFile(zip);
        zipFile.extractAll(destination);
        zip.delete();
        System.out.println("Unzip successful !!!");
    }
}
