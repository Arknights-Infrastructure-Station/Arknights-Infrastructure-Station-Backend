package com.milkwind.infrastructurestation.arknightsinfrastructurestationbackend;

import com.arknightsinfrastructurestationbackend.projectUtil.SecretKeyGenerator;
import lombok.Data;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.NoSuchAlgorithmException;

@SpringBootTest
public class SecretKeyTests {
    @Value("${custom-data.url}")
    private String url;

    @Test
    public void testGenerateSecretKey() throws NoSuchAlgorithmException {
        SecretKeyGenerator.generateSecretKey();
    }

    @Test
    public void printUrl(){
        System.out.println(url);
    }
}
