package com.leyou.cart.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {

    private String pubKeyPath;

    private String cookieName;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        System.out.println(pubKeyPath);
        System.out.println(cookieName);
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}

