package com.leyou.cart.service;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate template;

    private final static String KEY_PREFIX = "cart:uid";

    public void addCart(Cart cart) {
        // 获取登陆的用户
        UserInfo user = UserInterceptor.getUserInfo();
        // 得到 key
        String key = KEY_PREFIX + user.getId();
        String hashkey = cart.getSkuId().toString();
        // 判断当前购物车商品是否存在
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        if(operation.hasKey(hashkey)) {
            // 数量增加
            String json = operation.get(hashkey).toString();
            Cart cacheCart = JsonUtils.parse(json, Cart.class);
            cacheCart.setNum(cacheCart.getNum() + cart.getNum());
            operation.put(hashkey,JsonUtils.serialize(cacheCart));
        }else {
            // 添加
            operation.put(hashkey,JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCartList() {
        String key = KEY_PREFIX + UserInterceptor.getUserInfo().getId();
        if(!template.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        List<Object> values = operation.values();
        List<Cart> carts = values.stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取用户信息
        UserInfo user = UserInterceptor.getUserInfo();
        // 得到key和hashKey
        String key = KEY_PREFIX + user.getId();
        String hashKey = skuId.toString();
        // 绑定
        BoundHashOperations<String, Object, Object> operations = template.boundHashOps(key);
        if(!operations.hasKey(hashKey)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        String json = operations.get(hashKey).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        cart.setNum(num);
        // 把数据写回redis
        operations.put(hashKey,JsonUtils.serialize(cart));

    }

    public void deleteCart(Long skuId) {
        // 获取用户信息
        UserInfo user = UserInterceptor.getUserInfo();
        // 得到key和hashKey
        String key = KEY_PREFIX + user.getId();
        template.opsForHash().delete(key,skuId.toString());
    }
}
