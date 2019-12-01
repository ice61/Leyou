package com.leyou.cart.interceptor;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor{

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        // 解析token
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 传递user
            tl.set(user);
            // 放行
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 此方法在试图渲染后才执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
    }

    public static UserInfo getUserInfo() {
        return tl.get();
    }
}
