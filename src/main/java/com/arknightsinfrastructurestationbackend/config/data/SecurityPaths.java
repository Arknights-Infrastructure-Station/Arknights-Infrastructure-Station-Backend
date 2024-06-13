package com.arknightsinfrastructurestationbackend.config.data;

import java.util.List;

public class SecurityPaths {
    public static final List<String> PROTECTED_PATHS = List.of(
            "/api/user/**",
            "/api/starRecord/**",
            "/api/recyclingWorkFile/**",
            "/api/stagingWorkFile/**",
            "/api/workFile/create",
            "/api/workFile/update",
            "/api/workFile/screenPostedWorkFileList"
    );
}
