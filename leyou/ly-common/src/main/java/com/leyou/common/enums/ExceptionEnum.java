package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    CATEGORY_NOT_FOUND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    BRAND_SAVE_ERROR(500,"服务器新增品牌失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品sku不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_ID_NOT_FOUND(400,"商品id不能为空"),
    INVALID_USER_DATA_TYPE(400,"用户的数据类型无效"),
    INVALID_VERIFY_CODE(400,"无效的验证码"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败"),
    UNAUTHORIZED(403,"未授权"),
    CART_NOT_FOUND(404,"购物车为空"),
    ;
    private int code;
    private String msg;
}

