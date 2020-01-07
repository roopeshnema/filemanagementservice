package com.learning.fileupload.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.learning.fileupload.filestorageclient.FileStorageClient;

@RestController
@RequestMapping("/v1/storage/")
public class FileUploadController {
	
	private FileStorageClient amazonClient;

	@Autowired
	FileUploadController(FileStorageClient amazonClient) {
		this.amazonClient = amazonClient;
	}
	
	@PostMapping("/users/{userId}/documents")
	public String uploadDocuments(@RequestPart(value = "file") MultipartFile file, @PathVariable String userId) throws IOException {
		return this.amazonClient.uploadFile(userId,file);
	}
	
	@GetMapping("/users/{userId}/documents/{documentName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] getDocuments(@PathVariable String userId, @PathVariable String documentName) throws IOException {
		S3Object s3Object = this.amazonClient.getDocuments(userId,documentName);
		
		 InputStream in = s3Object.getObjectContent();
		    byte[] byteArray = IOUtils.toByteArray(in);
		    in.close();

		    return byteArray;
		
	}
	
//	@DeleteMapping("/deleteFile")
//	public String deleteFile(@RequestPart(value = "url") String fileUrl) {
//		return this.amazonClient.deleteFileFromS3Bucket(fileUrl);
//	}
	
	
	

}
