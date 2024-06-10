package com.milkwind.infrastructurestation.arknightsinfrastructurestationbackend;

import com.arknightsinfrastructurestationbackend.common.tools.JsonWorkProcessor;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ArknightsInfrastructureStationBackendApplicationTests {

    // 测试调换成功的情况
    @Test
    public void testExchangeSuccess() throws JSONException {
        String originalJson = "{\"plan1\": {\"room_1_1\": {\"name\": \"贸易站\"}, \"room_1_2\": {\"name\": \"制造站\"}}}";
        String exchangeRequest = "{\"order\": {\"room_1_1\": \"制造站\", \"room_1_2\": \"贸易站\"}}";
        OperateResult result = JsonWorkProcessor.exchangeRoomDataForMower(originalJson, exchangeRequest);
        JSONObject resultJson = null;
        resultJson = new JSONObject(result.getResult());
        assertTrue(resultJson.getBoolean("hasExchange"));
    }

    // 测试“待调换JSON数据”的制造站数量少于“调换要求”
    @Test
    public void testLessManufacturingStationsThanRequired() throws JSONException {
        String originalJson = "{\"plan1\": {\"room_1_1\": {\"name\": \"贸易站\"}, \"room_1_2\": {\"name\": \"制造站\"}}}";
        String exchangeRequest = "{\"order\": {\"room_1_1\": \"制造站\", \"room_1_2\": \"制造站\", \"room_1_3\": \"制造站\"}}";
        OperateResult result = JsonWorkProcessor.exchangeRoomDataForMower(originalJson, exchangeRequest);
        JSONObject resultJson = new JSONObject(result.getResult());
        assertFalse(resultJson.getBoolean("hasExchange"));
    }

    // 测试“待调换JSON数据”的发电站数量多于“调换要求”
    @Test
    public void testMorePowerStationsThanRequired() throws JSONException {
        String originalJson = "{\"plan1\": {\"room_1_1\": {\"name\": \"发电站\"}, \"room_1_2\": {\"name\": \"发电站\"}, \"room_1_3\": {\"name\": \"发电站\"}}}";
        String exchangeRequest = "{\"order\": {\"room_1_1\": \"发电站\", \"room_1_2\": \"发电站\"}}";
        OperateResult result = JsonWorkProcessor.exchangeRoomDataForMower(originalJson, exchangeRequest);
        JSONObject resultJson = new JSONObject(result.getResult());
        assertFalse(resultJson.getBoolean("hasExchange"));
    }

    // 测试“调换要求”给出了不存在于“待调换JSON数据”中的设施
    @Test
    public void testRequestContainsNonExistentFacility() throws JSONException {
        String originalJson = "{\"plan1\": {\"room_1_1\": {\"name\": \"贸易站\"}, \"room_1_2\": {\"name\": \"制造站\"}}}";
        String exchangeRequest = "{\"order\": {\"room_1_1\": \"研究所\", \"room_1_2\": \"贸易站\"}}";
        OperateResult result = JsonWorkProcessor.exchangeRoomDataForMower(originalJson, exchangeRequest);
        JSONObject resultJson = new JSONObject(result.getResult());
        assertFalse(resultJson.getBoolean("hasExchange"));
    }

    // 测试“待调换JSON数据”包含“调换要求”中没有的设施的情况
    @Test
    public void testJsonContainsFacilityNotInRequest() throws JSONException {
        String originalJson = "{\"plan1\": {\"room_1_1\": {\"name\": \"贸易站\"}, \"room_1_2\": {\"name\": \"制造站\"}, \"room_1_3\": {\"name\": \"发电站\"}}}";
        String exchangeRequest = "{\"order\": {\"room_1_1\": \"制造站\", \"room_1_2\": \"贸易站\"}}";
        OperateResult result = JsonWorkProcessor.exchangeRoomDataForMower(originalJson, exchangeRequest);
        JSONObject resultJson = new JSONObject(result.getResult());
        assertFalse(resultJson.getBoolean("hasExchange"));
    }
}
