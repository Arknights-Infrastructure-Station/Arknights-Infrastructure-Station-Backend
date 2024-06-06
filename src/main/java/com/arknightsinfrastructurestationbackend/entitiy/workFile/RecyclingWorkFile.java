package com.arknightsinfrastructurestationbackend.entitiy.workFile;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`recycling_work_files`")
public class RecyclingWorkFile {
    private Long id; //作业ID

    private String author; //作业作者

    private Long authorId; //作业作者的ID

    private String name; //作业名称，非空

    private String type; //作业类型，非空

    private String layout; //作业采用的基建布局，非空

    private String releaseDate; //作业发布时间，非空，该字符串的格式已被getCurrentDateTime设定为适配MySQL的datetime格式

    private String description; //作业描述

    private String descriptionPictures; //作业描述图片数组，JSON字符串格式，适配MySQL数据类型

    private String storageType; //作业内容存储类型

    private String fileContent; //作业文件内容，非空

    private String fileRequest; //作业文件要求，由前端设定，包括基建设施排布顺序（Mower限定）、干员精英化要求

    private Integer starNumber; //作业收藏数量，还原的时候保持不变

    private Integer downloadNumber; //作业下载数量，还原的时候保持不变

    private Float score; //作业评分，若为-1则表示暂无评分

    private String clearTime; //作业清除时间，后端会根据这个清除时间定时清除指定的作业记录

    public WorkFile toWorkFile() {
        return new WorkFile(id, author,
                authorId, name,
                type, layout,
                releaseDate, description,
                descriptionPictures, storageType,
                fileContent, fileRequest,
                starNumber, downloadNumber, score);
    }
}
