package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by lyz on 6/11/18.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID()+"."+fileExtensionName;
        logger.info("文件开始上传，上传的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //创建文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        //创建完整的文件
        File targetFile = new File(path,uploadFileName);
        try {
            //把传过来的文件写入创建的文件
            file.transferTo(targetFile);
            //文件已经上传到tomcat的文件夹上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //文件已经上传到ftp服务器上
            targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传异常",e);
        }

        return targetFile.getName();
    }
}
