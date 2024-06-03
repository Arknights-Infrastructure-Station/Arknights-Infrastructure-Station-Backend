package com.arknightsinfrastructurestationbackend.controller.workFile;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.dto.info.AdaptInfo;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adapter")
@AllArgsConstructor
@ExcludeFromTokenRefresh
public class AdapterController {
    private final AdapterService adapterService;

    /**
     * 让Mower作业适配用户的基建排布配置
     *
     * @param adaptInfo 适配信息
     * @return 适配后的作业
     */
    @PostMapping("/getAdaptedMower")
    public ResponseEntity<Object> getAdaptedMower(@RequestBody AdaptInfo adaptInfo) {
        return ResponseEntity.ok(adapterService.mowerAdapt(adaptInfo.getSource(), adaptInfo.getRequire()));
    }
}
