package com.leyou.item.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.sound.midi.Soundbank;
import java.beans.Transient;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private AmqpTemplate amqpTemplate;


    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤
        if(StringUtils.isNotBlank(key)) {
            criteria.andLike("title","%"+ key +"%");
        }
        //上下架过滤
        if(saleable != null) {
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        List<Spu> list = spuMapper.selectByExample(example);
        //判断
        if(CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        loadCategoryAndBrandName(list);
        PageInfo<Spu> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(),list);
    }

    private void loadCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(c -> c.getName()).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }
    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        // 新增sku和库存
        saveSkuAndStock(spu);
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    private void saveSkuAndStock(Spu spu) {
        int count;List<Stock> stockList = new LinkedList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if(count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
        // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        stockMapper.insertList(stockList);
    }

    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if(spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> list = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        // 查询库存
        loadStockInSku(list);
        return list;
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId() == null) {
            throw new LyException(ExceptionEnum.GOODS_ID_NOT_FOUND);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        // 修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 新增sku和stock
        saveSkuAndStock(spu);
        //发送MQ消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 查询sku
        spu.setSkus(querySkuBySpuId(id));
        // 查询detail
        spu.setSpuDetail(querySpuDetailById(id));
        return spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSku(skus);
        return skus;
    }

    private void loadStockInSku(List<Sku> skus) {
        for(Sku sku1 : skus) {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            if(stock == null) {
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            sku1.setStock(stock.getStock());
        }
    }
}
