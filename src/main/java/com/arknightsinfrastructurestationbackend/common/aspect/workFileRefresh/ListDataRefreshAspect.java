package com.arknightsinfrastructurestationbackend.common.aspect.workFileRefresh;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.adapter.WorkFileInterface;
import com.arknightsinfrastructurestationbackend.global.type.RefreshType;
import com.arknightsinfrastructurestationbackend.service.workFile.refresh.RefreshWorkFileDataService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@AllArgsConstructor
public class ListDataRefreshAspect {
    private final RefreshWorkFileDataService refreshWorkFileDataService;

    @Around("@annotation(listDataRefresh)")
    public <W extends WorkFileInterface> List<W> around(ProceedingJoinPoint joinPoint, ListDataRefresh listDataRefresh) throws Throwable {
        // 获取注解参数
        RefreshType[] params = listDataRefresh.value();

        // 调用原方法
        @SuppressWarnings("unchecked") //只有返回值类型为List<WorkFile>或List<StagingWorkFile>才会标注这个注解
        List<W> workFileList = (List<W>) joinPoint.proceed();

        for (RefreshType param : params) {
            if (param == RefreshType.DSS) {
                //配置DS属性的只会是返回值类型为List<WorkFile>的方法
                refreshWorkFileDataService.refreshWorkFileData((List<WorkFile>) workFileList);
            }
            //PK暂时禁用，图片value的获取改为懒加载
//            if (param == RefreshType.PK) {
//                //返回值为List<WorkFile>和List<StagingWorkFile>的方法均有可能配置PK属性
//                refreshWorkFileDataService.recoverPictureKey(workFileList);
//            }
        }

        return workFileList;
    }
}
