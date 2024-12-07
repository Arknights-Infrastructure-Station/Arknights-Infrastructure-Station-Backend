package com.arknightsinfrastructurestationbackend.controller.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.StarRecordAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.StarRecord;
import com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser.StarRecordAdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adminApi/starRecord")
@AllArgsConstructor
public class StarRecordAdminController {
    private final StarRecordAdminService starRecordAdminService;

    /**
     * 根据管理员设置的筛选参数查询出收藏作业记录列表
     *
     * @param starRecordAdminScreen 管理员的收藏记录筛选参数
     * @return 收藏作业记录列表
     */
    @PostMapping("/screenStarRecordList")
    public ResponseEntity<Object> screenStarRecordList(@RequestBody StarRecordAdminScreen starRecordAdminScreen) {
        List<StarRecord> starRecords = starRecordAdminService.screenStarRecordList(starRecordAdminScreen);
        return ResponseEntity.ok(new OperateAndStarListResult(new OperateResult(200, "收藏记录列表获取成功"),
                FileConverter.B2FSR(starRecords)));
    }

    /**
     * 删除收藏记录
     *
     * @param fStarRecord 包含 wid 和 uid 的收藏记录对象
     * @return 删除操作结果
     */
    @PostMapping("/deleteStarRecord")
    public ResponseEntity<Object> deleteStarRecord(@RequestBody F_StarRecord fStarRecord) {
        OperateResult result;
        try {
            Long wid = Long.parseLong(fStarRecord.getWid());
            Long uid = Long.parseLong(fStarRecord.getUid());
            result = starRecordAdminService.deleteStarRecord(wid, uid);
        } catch (NumberFormatException e) {
            result = new OperateResult(500, "参数格式错误：" + e.getMessage());
        }
        return ResponseEntity.ok(new OperateResult(result.getOperateCode(), result.getMessage()));
    }

    private record OperateAndStarListResult(OperateResult operateResult, List<F_StarRecord> starList) {
    }
}

