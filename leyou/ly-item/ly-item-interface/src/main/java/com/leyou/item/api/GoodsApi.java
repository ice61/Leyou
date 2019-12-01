package com.leyou.item.api;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    @GetMapping("/spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page",defaultValue="1")Integer page,
            @RequestParam(value = "rows",defaultValue="5")Integer rows,
            @RequestParam(value = "saleable",required = false)boolean saleable,
            @RequestParam(value = "key",required = false)String key
    );

    @PostMapping("goods")
    void saveGoods(@RequestBody Spu spu);

    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);

    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    @PutMapping("/goods")
    void updateGoods(@RequestBody Spu spu);

    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);
}
