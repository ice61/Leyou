package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Service
public class UploadService {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");

    public String uploadImage(MultipartFile file) {

        try {
            //首先我们通过后缀名校验文件的类型
            String contentType = file.getContentType();
            if(!ALLOW_TYPES.contains(contentType)) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //然后我们通过校验文件内容判断文件类型
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),file.getSize(),extension,null);

            return "http://image.leyou.com/" + storePath.getFullPath();

        } catch (IOException e) {
            log.error("上传文件失败",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
