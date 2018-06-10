package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by lyz on 6/7/18.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int countResult = userMapper.checkUsername(username);
        //用户名校验
        if (countResult == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5password);
        //密码校验，存在用户，and条件没有链接上
        if (user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //密码改为空，安全
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccessMessage("登录成功",user);
    }

    @Override
    public ServerResponse<String> register(User user) {
       /* int countResult = userMapper.checkUsername(user.getUsername());
        //用户名校验
        if (countResult > 0){
            return ServerResponse.createByErrorMessage("用户名已存在");
        }*/

        //复用checkValid代码
        ServerResponse serverResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!serverResponse.isSuccess()){
            return  serverResponse;
        }

       /* countResult = userMapper.checkEmail(user.getEmail());
        //email校验
        if (countResult > 0){
            return ServerResponse.createByErrorMessage("邮箱已存在");
        }*/

        //复用checkValid代码
        serverResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!serverResponse.isSuccess()){
            return  serverResponse;
        }


        //对手机号码的校验
        // TODO: 6/7/18

        //默认为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5对password进行加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int insertResult = userMapper.insert(user);
        if (insertResult == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str,String type){
        //先进行str的非空校验，notblanl("  " false) and notempty("  "  true),
        if (StringUtils.isNotBlank(str)){
            if (Const.USERNAME.equals(type)){
                int countResult = userMapper.checkUsername(str);
                //用户名校验
                if (countResult > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)){
                int countResult = userMapper.checkEmail(str);
                //email校验
                if (countResult > 0){
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }
        else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        //用户名不存在
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            //问题和答案是这个用户的，并且答案是正确的
            String forgetToken = UUID.randomUUID().toString();
            //把token放入缓存，设置了保存12小时
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        //判断token是否为空
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token参数没有传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        //用户名不存在
        if (validResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //从缓存中取出token做判断
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者已经过期");
        }

        //使用StringUtils的equals方法比object的好
        if (StringUtils.equals(forgetToken,token)){
            //重置用户密码
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username,md5Password);
            if (resultCount > 0){
                return ServerResponse.createBySuccessMessage("更新密码成功");
            }
            //return ServerResponse.createByErrorMessage("更新密码出错");
        }
        else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码出错");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //需要校验这个旧的密码是否属于这个用户，避免发生横向越权问题
        Integer userId = user.getId();
        String password = MD5Util.MD5EncodeUtf8(passwordOld);
        int resultCount = userMapper.checkPassword(password,userId);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码出错");
    }

    @Override
    public ServerResponse<User> updateInformation(User user){
        //username不能被更新
        //email校验是否已经存在，如果存在不能是本用户的

        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0){
            return ServerResponse.createByErrorMessage("该邮箱已经存在，请更换邮箱后再更新");
        }

        //声明一个新的user，用于更新信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setEmail(user.getEmail());
        //update_time数据库自动更新

        resultCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (resultCount > 0){
            //此处不应该传递的是updateUser，
            return ServerResponse.createBySuccessMessage("信息个人更新成功",user);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("未找到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend

    /**
     * 校验是否管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user){
        if (user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
