package com.arknightsinfrastructurestationbackend.entitiy.workFile;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.adapter.WorkFileInterface;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发布的作业
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`work_files`")
public class WorkFile implements WorkFileInterface {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; //作业ID

    private String author; //作业作者

    private Long authorId; //作业作者的ID

    private String name; //作业名称，非空

    private String type; //作业类型，非空

    private String layout; //作业采用的基建布局，非空

    private String releaseDate; //作业发布时间，非空，该字符串的格式已被getCurrentDateTime设定为适配MySQL的datetime格式

    private String description; //作业描述

    private String descriptionPictures; //作业描述图片数组，JSON字符串格式，适配MySQL数据类型

    private String storageType; //作业内容存储类型，分为纯文本存储和图片文件key存储

    private String fileContent; //作业文件内容（纯文本或是图片文件key），非空

    private String fileRequest; //作业文件要求，由前端设定，包括基建设施排布顺序（Mower限定）、干员精英化要求

    private Integer starNumber; //作业收藏数量，创建作业的时候默认为0（因为作业收藏记录表中没有相应的记录）

    private Integer downloadNumber; //作业下载数量，创建作业的时候默认为0

    private Float score; //作业评分，若为-1则表示该作业暂无评分

    public RecyclingWorkFile toRecyclingWorkFile() {
        return new RecyclingWorkFile(id, author,
                authorId, name,
                type, layout,
                releaseDate, description,
                descriptionPictures, storageType,
                fileContent, fileRequest,
                starNumber, downloadNumber,
                score,null);
    }
}
