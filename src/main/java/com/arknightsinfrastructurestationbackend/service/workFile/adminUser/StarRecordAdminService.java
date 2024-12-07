package com.arknightsinfrastructurestationbackend.service.workFile.adminUser;

import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.StarRecordAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.commonUser.StarRecord;
import com.arknightsinfrastructurestationbackend.mapper.commonUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StarRecordAdminService {
    private final StarRecordMapper starRecordMapper;
    private final AdapterService adapterService;

    public OperateResult deleteStarRecord(Long wid, Long uid) {
        LambdaQueryWrapper<StarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StarRecord::getWid, wid)
                .eq(StarRecord::getUid, uid);
        StarRecord existingRecord = starRecordMapper.selectOne(queryWrapper);
        if (existingRecord == null) {
            return new OperateResult(404, "收藏记录不存在");
        }

        if (starRecordMapper.delete(queryWrapper) > 0) {
            return new OperateResult(200, "取消收藏成功");
        } else {
            return new OperateResult(500, "取消收藏失败");
        }
    }

    public List<StarRecord> screenStarRecordList(StarRecordAdminScreen starRecordAdminScreen) {
        QueryWrapper<StarRecord> queryWrapper = adapterService.createAdminStarRecordQueryWrapper(starRecordAdminScreen);
        return starRecordMapper.selectList(queryWrapper);
    }
}

