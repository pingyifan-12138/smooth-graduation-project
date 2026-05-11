package com.manage.club.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Component
public class FileUtil {
    @Value("${upload.base-path}")
    private String basePath;

    /**
     * 上传图片（头像/封面图）
     */
    public String uploadImage(MultipartFile file) {
        try {
            // 1. 创建上传目录
            File dir = new File(basePath);
            if (!dir.exists()) dir.mkdirs();

            // 2. 生成唯一文件名（避免重复）
            String originalName = file.getOriginalFilename();
            String suffix = FilenameUtils.getExtension(originalName);
            String fileName = UUID.randomUUID().toString() + "." + suffix;

            // 3. 保存文件
            File dest = new File(basePath + fileName);
            file.transferTo(dest);

            // 4. 返回访问路径（前端直接拼接baseUrl）
            return "/uploads/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}