package com.arknightsinfrastructurestationbackend.config.data;

import java.util.List;

public class SecurityPaths {
    public static final List<String> USER_PATHS = List.of(
            "/api/user/",
            "/api/starRecord/",
            "/api/recyclingWorkFile/",
            "/api/stagingWorkFile/",
            "/api/workFile/create",
            "/api/workFile/update",
            "/api/workFile/screenPostedWorkFileList"
    );
    public static final List<String> ADMIN_PATHS = List.of(
            "/adminApi/"
    );
}
