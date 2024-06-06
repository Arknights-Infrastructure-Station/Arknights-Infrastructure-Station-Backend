package com.arknightsinfrastructurestationbackend.entitiy.store;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 哈夫曼解压缩Map存储
 */
@Data
@TableName("`huffman_map`")
@AllArgsConstructor
public class HuffmanMap {
    private Long wid;
    private String map;
}
