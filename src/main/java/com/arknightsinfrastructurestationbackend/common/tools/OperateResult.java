package com.arknightsinfrastructurestationbackend.common.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//用户操作结果对象
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperateResult {
    private Integer operateCode;
    private String message;
    private String result;

    public OperateResult(Integer operateCode, String message) {
        this.operateCode = operateCode;
        this.message = message;
    }

    public boolean isOk() {
        return this.operateCode >= 200 && this.operateCode < 300;
    }

    public boolean isRight() {
        return this.operateCode == 200;
    }

    public boolean isError(){
        return this.operateCode >= 400;
    }
}
