package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by lyz on 6/10/18.
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    /**
     * 增加或更新产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "/product_save.do")
    @ResponseBody
    public ServerResponse<String> productSave(HttpSession session, Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充添加产品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新产品上下架状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "/set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充业务逻辑
            return iProductService.setSaleStatus(productId,status);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 管理产品详细信息
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "/detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充业务逻辑
            return iProductService.manageProductDetail(productId);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查询产品的list，进行分页展示
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getList(HttpSession session
            , @RequestParam(value = "pageNum",defaultValue = "1") int pageNum
            , @RequestParam(value = "pageSize",defaultValue = "10") int pageSize ){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充业务逻辑
            return iProductService.getList(pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 产品搜索
     * @param session
     * @param productId
     * @param productName
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,Integer productId,String productName
            , @RequestParam(value = "pageNum",defaultValue = "1") int pageNum
            , @RequestParam(value = "pageSize",defaultValue = "10") int pageSize ){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充业务逻辑
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 文件上传
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "/upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpSession session, HttpServletRequest request
            ,@RequestParam(value = "upload_file",required = false) MultipartFile file){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录后操作");
        }
        ServerResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            //填充业务逻辑
            //在webapp下创建upload文件夹
            String path = request.getSession().getServletContext().getRealPath("upload");
            //把文件上传到该文件夹
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);

        }else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }

    }

    /**
     * 富文本上传，simditor格式返回
     * @param session
     * @param request
     * @param response
     * @param file
     * @return
     */
    @RequestMapping(value = "/richtext_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, HttpServletRequest request,HttpServletResponse response
            ,@RequestParam(value = "upload_file",required = false) MultipartFile file){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map resultMap = Maps.newHashMap();
        if (user == null){
            resultMap.put("success",false);
            resultMap.put("msg","用户未登录，请登录后操作");
            return resultMap;
        }
        ServerResponse serverResponse = iUserService.checkAdminRole(user);
        if (serverResponse.isSuccess()){
            //填充业务逻辑
            //在webapp下创建upload文件夹
            String path = request.getSession().getServletContext().getRealPath("upload");
            //把文件上传到该文件夹
            String targetFileName = iFileService.upload(file,path);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            //按照simditor要求格式进行返回
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            //添加前端插件需要后端的返回格式
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;

        }else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作，需要管理员权限");
            return resultMap;
        }

    }

}
