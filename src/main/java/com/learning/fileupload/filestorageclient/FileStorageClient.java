package com.learning.fileupload.filestorageclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

@Service
public class FileStorageClient {
	
	private AmazonS3 s3client;

	@Value("${StorageProperties.endpointUrl}")
	private String endpointUrl;
	@Value("${StorageProperties.bucketName}")
	private String bucketName;
	@Value("${StorageProperties.accessKey}")
	private String accessKey;
	@Value("${StorageProperties.secretKey}")
	private String secretKey;
	
	private Logger logger = LoggerFactory.getLogger(FileStorageClient.class);
	
	// Sample URL
	// https://s3.us-east-1.amazonaws.com/eventapp-fileupload-demo/1578024943881-Blogging_Tips_LinkedIn_Post_Header_(1).png
	// {endpointURL}/{bucketName}/{fileName}
	
	
	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = AmazonS3ClientBuilder
				  .standard()
				  .withCredentials(new AWSStaticCredentialsProvider(credentials))
				  .withRegion(Regions.US_EAST_2)
				  .build();
	}
	
	public String uploadFile(String userId, MultipartFile multipartFile) {
		
		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			String fileName = generateFileName(multipartFile);
			fileUrl = this.endpointUrl + "/" + this.bucketName + "/" + userId + "/" + fileName;
			PutObjectResult result = uploadFileTos3bucket(fileName, file, userId);
			file.delete();
		} /*catch (Exception e) {
			e.printStackTrace();
		}*/
		catch (AmazonServiceException ase) {
	          logger.info("Caught an AmazonServiceException from GET requests, rejected reasons:");
	          logger.info("Error Message:    " + ase.getMessage());
	          logger.info("HTTP Status Code: " + ase.getStatusCode());
	          logger.info("AWS Error Code:   " + ase.getErrorCode());
	          logger.info("Error Type:       " + ase.getErrorType());
	          logger.info("Request ID:       " + ase.getRequestId());
	          
	          
	            } catch (AmazonClientException ace) {
	              logger.info("Caught an AmazonClientException: ");
	                logger.info("Error Message: " + ace.getMessage());
	            } catch (IOException ioe) {
	              logger.info("IOE Error Message: " + ioe.getMessage());
	              
	            }
		return fileUrl;
	}
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
	
	private String generateFileName(MultipartFile multiPart) {
		return multiPart.getOriginalFilename().replace(" ", "_");
	}
	
	private  PutObjectResult uploadFileTos3bucket(String fileName, File file, String key) {
		return this.s3client.putObject(
				new PutObjectRequest(this.bucketName, key + "/" + fileName, file));
				//.withCannedAcl(CannedAccessControlList.PublicRead));
	}
	
	public S3Object getDocuments(String userId, String fileName) {
		
		String key = userId + "/" + userId + "_profileimage.png";
		return  this.s3client.getObject(new GetObjectRequest(bucketName, key  ));
		
	}
	
//	public String deleteFileFromS3Bucket(String fileName) {
//		
//		String fileName = endpointUrl + fileName
//		s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
//		return "Successfully deleted";
//	}
	
}
