package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    // 登陆授权功能
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username, @RequestParam("password") String password,
            HttpServletResponse response, HttpServletRequest request) {
        String token = authService.login(username, password);
        // 写入cookie
        CookieUtils.setCookie(request, response, prop.getCookieName(), token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token, HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isBlank(token)) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        try {
            // 解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 刷新token生存时间
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            CookieUtils.setCookie(request, response, prop.getCookieName(), newToken);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            // token未授权或已经过期
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
