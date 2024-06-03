package com.arknightsinfrastructurestationbackend.common.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class JsonWorkProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 该方法用于根据指定的调换要求，交换Mower的JSON作业中特定房间节点的数据。
     *
     * @param jsonData        待调换的JSON数据字符串。
     * @param exchangeRequest 调换要求的JSON数据字符串。
     * @return 返回调换后的JSON数据字符串。如果调换成功，hasExchange为true；如果失败，hasExchange为false。
     */
    public static OperateResult exchangeRoomDataForMower(String jsonData, String exchangeRequest) {
//        Log.info("________________");
//        Log.info(jsonData);
//        Log.info(exchangeRequest);
        JSONObject originalJson = new JSONObject(jsonData);
        JSONObject orderJson = new JSONObject(exchangeRequest);
//        Log.info(originalJson.toString());
//        Log.info(orderJson.toString());

        // 步骤1：从originalJson中获取plan1属性下的所有以“room”开头的属性的name属性
        JSONObject plan1 = originalJson.getJSONObject("plan1");
        Set<String> plan1KetSetInOrigin = plan1.keySet();
        List<String> compareArray1 = new ArrayList<>();
        for (String key : plan1KetSetInOrigin) {
            if (key.startsWith("room")) {
                compareArray1.add(plan1.getJSONObject(key).getString("name"));
            }
        }

        // 步骤2：从orderJson中获取order属性下的所有value
        JSONObject orderList = orderJson.getJSONObject("order");
        Set<String> keySet = orderJson.getJSONObject("order").keySet();
        List<String> compareArray2 = new ArrayList<>();
        for (String key : keySet) {
            compareArray2.add(orderList.getString(key));
        }
//        Log.info(compareArray1.toString());
//        Log.info(compareArray2.toString());
        // 步骤3：检查作业基建布局和用户基建布局是否一开始就相等
        boolean isEqual = compareArray1.equals(compareArray2);
        if (isEqual)
            return new OperateResult(201, "基建布局相同，无需适配", jsonData);

        // 步骤4：如果基建布局一开始不相等，排序并比较两个数组，以检查房间名称是否匹配
        Collections.sort(compareArray1);
        Collections.sort(compareArray2);
        isEqual = compareArray1.equals(compareArray2);

        // 步骤5：如果房间名称不匹配，返回未进行调换的原始JSON数据
        if (!isEqual) {
            return new OperateResult(500, "基建布局不同，无法适配", originalJson.toString(0));
        } else {
            // 步骤6：如果房间名称匹配，将原始房间数据存储到Map中
            Map<String, JSONObject> storeRooms = new HashMap<>();
            for (String key : plan1KetSetInOrigin) {
                if (key.startsWith("room")) {
                    storeRooms.put(key, plan1.getJSONObject(key));
                }
            }

            // 步骤7：遍历调换要求，根据要求调换房间
            for (String key : keySet) {
                String roomName = orderList.getString(key); // 获取调换要求中的房间名称
                for (Map.Entry<String, JSONObject> entry : storeRooms.entrySet()) {
                    if (entry.getValue().getString("name").equals(roomName)) {
                        plan1.put(key, new JSONObject(entry.getValue().toString(0))); // 更新originalJson中相应的房间数据
                        storeRooms.remove(entry.getKey()); // 从存储中移除已经匹配的房间
                        break; // 匹配成功后跳出内部循环
                    }
                }
            }

            // 步骤8：构建并返回调换后的JSON数据
            return new OperateResult(200, "适配成功", originalJson.toString(0));
        }
    }

    /**
     * 将装有JSON对象的List转换为JSON格式字符串
     */
    public static <T> String convertListToJson(List<T> list) throws IOException {
        return objectMapper.writeValueAsString(list);
    }
}
