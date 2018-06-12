package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by lyz on 6/10/18.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService{
    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public ServerResponse<String> addCategory(String categoryName,Integer parentId){
        if (categoryName == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setStatus(true);//这个分类是可以用的
        category.setParentId(parentId);

        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse<String> updateCategoryName(Integer categoryId,String categoryName){
        if (categoryName == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("修改品类参数错误");
        }
        Category updateCategory = new Category();
        updateCategory.setId(categoryId);
        updateCategory.setName(categoryName);

        int resultCount = categoryMapper.updateByPrimaryKeySelective(updateCategory);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createByErrorMessage("更新品类失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        //isEmpty --> null and size=0 没找到而已，并不是错误，需要正常返回
        if (CollectionUtils.isEmpty(categoryList)){
            //打印本类的日志
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询节点id和子节点id
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        //gavae set
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet,categoryId);

        //处理结果set
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null){
            for (Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return  ServerResponse.createBySuccess(categoryIdList);
    }

    //注意set去重的问题，重写equals和hashCode方法
    //递归算法，算出子节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet,Integer categoryId){
        //先找父节点，加入set中
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }
        //查找子节点，递归算法一定要有退出条件
        //查找子节点，加入List中，然后再遍历，再查找子节点
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        for (Category categoryItem : categoryList){
            findChildrenCategory(categorySet,categoryItem.getId());
        }
        //递归出口，就是结束上面的for循环，也就是categoryList为空，不进入循环，return结束方法
        return categorySet;
    }

}
