package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by lyz on 6/11/18.
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
