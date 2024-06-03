package com.arknightsinfrastructurestationbackend.common.tools;

import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StagingWorkFile;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StarRecord;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.user.StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class FileConverter {
    private static void copyProperties(Object source, Object target) {
        Field[] sourceFields = source.getClass().getDeclaredFields();
        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            Object value;
            try {
                value = sourceField.get(source);
                Field targetField;
                try {
                    targetField = target.getClass().getDeclaredField(sourceField.getName());
                } catch (NoSuchFieldException e) {
                    continue; // 如果目标对象没有相同名称的字段，则跳过
                }

                targetField.setAccessible(true);

                // 类型转换逻辑
                if (sourceField.getType().equals(String.class) && targetField.getType().equals(Long.class)) {
                    // 从 String 转换到 Long
                    value = value != null ? Long.parseLong((String) value) : null;
                } else if (sourceField.getType().equals(Long.class) && targetField.getType().equals(String.class)) {
                    // 从 Long 转换到 String
                    value = value != null ? String.valueOf(value) : null;
                }

                targetField.set(target, value);
            } catch (IllegalAccessException e) {
                Log.error(e.getMessage());
            }
        }
    }

    public static WorkFile FB(F_WorkFile fWorkFile) {
        if (fWorkFile == null) {
            return null;
        }
        WorkFile workFile = new WorkFile();
        copyProperties(fWorkFile, workFile);
        return workFile;
    }

    public static F_WorkFile FB(WorkFile workFile) {
        if (workFile == null) {
            return null;
        }
        F_WorkFile fWorkFile = new F_WorkFile();
        copyProperties(workFile, fWorkFile);
        return fWorkFile;
    }

    public static StagingWorkFile FB(F_StagingWorkFile fStagingWorkFile) {
        if (fStagingWorkFile == null) {
            return null;
        }
        StagingWorkFile stagingWorkFile = new StagingWorkFile();
        copyProperties(fStagingWorkFile, stagingWorkFile);
        return stagingWorkFile;
    }

    public static F_StagingWorkFile FB(StagingWorkFile stagingWorkFile) {
        if (stagingWorkFile == null) {
            return null;
        }
        F_StagingWorkFile fStagingWorkFile = new F_StagingWorkFile();
        copyProperties(stagingWorkFile, fStagingWorkFile);
        return fStagingWorkFile;
    }

    public static RecyclingWorkFile FB(F_RecyclingWorkFile fRecyclingWorkFile) {
        if (fRecyclingWorkFile == null) {
            return null;
        }
        RecyclingWorkFile recyclingWorkFile = new RecyclingWorkFile();
        copyProperties(fRecyclingWorkFile, recyclingWorkFile);
        return recyclingWorkFile;
    }

    public static F_RecyclingWorkFile FB(RecyclingWorkFile recyclingWorkFile) {
        if (recyclingWorkFile == null) {
            return null;
        }
        F_RecyclingWorkFile fRecyclingWorkFile = new F_RecyclingWorkFile();
        copyProperties(recyclingWorkFile, fRecyclingWorkFile);
        return fRecyclingWorkFile;
    }

    public static StarRecord FB(F_StarRecord fStarRecord) {
        if (fStarRecord == null) {
            return null;
        }
        StarRecord starRecord = new StarRecord();
        copyProperties(fStarRecord, starRecord);
        return starRecord;
    }

    public static F_StarRecord FB(StarRecord starRecord) {
        if (starRecord == null) {
            return null;
        }
        F_StarRecord fStarRecord = new F_StarRecord();
        copyProperties(starRecord, fStarRecord);
        return fStarRecord;
    }

    public static List<WorkFile> F2BWL(List<F_WorkFile> fWorkFileList) {
        if (fWorkFileList == null) {
            return null;
        }
        return fWorkFileList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<F_WorkFile> B2FWL(List<WorkFile> workFileList) {
        if (workFileList == null) {
            return null;
        }
        return workFileList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<StagingWorkFile> F2BSWL(List<F_StagingWorkFile> fStagingWorkFileList) {
        if (fStagingWorkFileList == null) {
            return null;
        }
        return fStagingWorkFileList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<F_StagingWorkFile> B2FSWL(List<StagingWorkFile> stagingWorkFiles) {
        if (stagingWorkFiles == null) {
            return null;
        }
        return stagingWorkFiles.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<RecyclingWorkFile> F2BRWL(List<F_RecyclingWorkFile> fRecyclingWorkFileList) {
        if (fRecyclingWorkFileList == null) {
            return null;
        }
        return fRecyclingWorkFileList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<F_RecyclingWorkFile> B2FRWL(List<RecyclingWorkFile> recyclingWorkFiles) {
        if (recyclingWorkFiles == null) {
            return null;
        }
        return recyclingWorkFiles.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<StarRecord> F2BSR(List<F_StarRecord> fStarRecordList) {
        if (fStarRecordList == null) {
            return null;
        }
        return fStarRecordList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }

    public static List<F_StarRecord> B2FSR(List<StarRecord> starRecordList) {
        if (starRecordList == null) {
            return null;
        }
        return starRecordList.stream()
                .map(FileConverter::FB)
                .collect(Collectors.toList());
    }
}
