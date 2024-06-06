package com.arknightsinfrastructurestationbackend.dto.wrapperClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class F_RecyclingWorkFile {
    private String id; // 作业ID（String类型）
    private String author; // 作业作者
    private String authorId; // 作业作者的ID（String类型）
    private String name; // 作业名称
    private String type; // 作业类型
    private String layout; // 作业采用的基建布局
    private String releaseDate; // 作业发布时间
    private String description; // 作业描述
    private String descriptionPictures; //作业描述图片数组
    private String storageType; // 作业内容存储方式
    private String fileContent; // 作业文件内容
    private String fileRequest; // 作业文件要求
    private Integer starNumber; // 作业收藏数量
    private Integer downloadNumber; // 作业下载数量
    private Float Score; //作业评分
    private String clearTime; // 作业清除时间
}

