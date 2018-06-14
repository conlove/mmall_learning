package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by lyz on 6/14/18.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService{

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        //插入的shipping的id是没有的，配置自动插入后填充shipping的id
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccessMessage("创建地址成功",result);
        }
        return ServerResponse.createBySuccessMessage("创建地址失败");
    }

    @Override
    public ServerResponse del(Integer userId, Integer shippingId){
        //注意发生横向越权的问题，登录了的用户拿到任意shippingId既可删除别人的地址
        //int resultCount = shippingMapper.deleteByPrimaryKey(shippingId);
        int resultCount = shippingMapper.deleteByUserIdShippingId(userId,shippingId);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createBySuccessMessage("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping){
        //注意发生横向越权的问题，登录了的用户拿到任意shippingId既可update别人的地址
        //int resultCount = shippingMapper.updateByPrimaryKeySelective(shipping);

        //重新赋值id，防止越权更新
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateByUserId(userId,shipping);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createBySuccessMessage("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if (shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
