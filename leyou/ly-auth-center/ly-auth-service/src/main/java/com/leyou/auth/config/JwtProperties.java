package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {

    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private String cookieName;


    private PublicKey publicKey;

    private PrivateKey privateKey;

    // 对象实例化后，就应该读取公钥和私钥

    // 下面的注解，使方法在构造函数执行完毕后执行
    @PostConstruct
    public void init() throws Exception {
        File pubPath = new File(pubKeyPath);
        File priPath = new File(priKeyPath);
        if(!pubPath.exists() || !priPath.exists()) {
            // 如果不存在，就生成公钥和私钥
            RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
        }
        // 读取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

}
