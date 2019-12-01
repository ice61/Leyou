package com.leyou.auth;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.*;

public class JwtUtilsTest {

    private static final String pubKeyPath = "E:/rsa.pub";

    private static final String priKeyPath = "E:/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(30L, "wangwu"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MzAsInVzZXJuYW1lIjoid2FuZ3d1IiwiZXhw" +
                "IjoxNTUwNDk5MDY1fQ.j2ddpOvDSEYchfx8jguam6Sb3pMWuKJjM_nMGvkOxZEmmtDx_s7DdHU2o6y" +
                "-etb7E4Tapuen8SVKpi5VSm5gWEGhe9Iic4QDYCCzOAzGHKCX4QVCqdgi7B_Nmj69tY2uz7iPg1mSemtJ85p" +
                "z4m_JJZYTcUjJMNIUCJ32zX95aQs";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}