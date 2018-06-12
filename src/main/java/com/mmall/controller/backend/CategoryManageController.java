package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by lyz on 6/10/18.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 增加品类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    //分类没有传递父节点id，就判断为根节点
    @RequestMapping(value = "/add_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session,String categoryName
            ,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //调用UserService方法进行是否管理员的判断
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //权限是管理员，增加分类处理逻辑
            return iCategoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "/set_category_name.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session,Integer categoryId,String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //调用UserService方法进行是否管理员的判断
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //权限是管理员
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 获取分类的子分类，平级，不发生递归
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "/get_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //调用UserService方法进行是否管理员的判断
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //权限是管理员，
            //查询子节点category信息，并且不进行递归，保持平级
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查询分类id和子分类的id，递归查询
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "/get_deep_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //调用UserService方法进行是否管理员的判断
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //权限是管理员，
            //查询节点id和子节点id，需要进行递归，0-->1003-->10006-->100009
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

}
