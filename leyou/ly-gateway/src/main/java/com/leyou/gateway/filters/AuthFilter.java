package com.leyou.gateway.filters;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter{

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;
    }

    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = requestContext.getRequest();
        // 获取url路径
        String path = request.getRequestURI();
        boolean isAllowPath = isAllowPath(path);
        return !isAllowPath;
    }

    private boolean isAllowPath(String path) {
        for (String allowPath : filterProp.getAllowPaths()) {
            if(path.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        // 获取上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest request = requestContext.getRequest();
        // 获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        } catch (Exception e) {
            // 解析失败，未登录
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        }
        return null;
    }
}
