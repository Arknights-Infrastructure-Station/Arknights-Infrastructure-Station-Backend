package com.arknightsinfrastructurestationbackend.dto.wrapperClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class F_StagingWorkFile {
    private String id; // 作业ID（String类型）
    private String name; // 作业名称
    private String author; // 作业作者
    private String authorId; // 作业作者的ID（String类型）
    private String type; // 作业类型
    private String layout; // 作业采用的基建布局
    private String stagingDate; // 作业暂存时间
    private String description; // 作业描述
    private String descriptionPictures; //作业描述图片数组
    private String storageType; // 作业内容存储方式
    private String fileContent; // 作业文件内容
    private String fileRequest; // 作业文件要求
}
