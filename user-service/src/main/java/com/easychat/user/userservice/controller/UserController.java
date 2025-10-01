package com.easychat.user.userservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.advice.BaseController;
import com.easychat.common.api.AdminDubboService;
import com.easychat.common.config.AvatarConfig;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.entity.po.AppUpdate;
import com.easychat.common.entity.vo.PageResultVO;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisComponet;
import com.easychat.common.utils.RedisUtils;

import com.easychat.common.utils.UserContext;
import com.easychat.user.userservice.constant.Constants;
import com.easychat.user.userservice.entity.dto.UserFormDTO;
import com.easychat.user.userservice.entity.dto.UserInfoBeautyDTO;
import com.easychat.user.userservice.entity.dto.UserInfoDTO;
import com.easychat.user.userservice.entity.po.UserInfo;
import com.easychat.user.userservice.entity.po.UserInfoBeauty;
import com.easychat.user.userservice.entity.vo.AppUpdateVO;
import com.easychat.user.userservice.entity.vo.SearchResultVO;
import com.easychat.user.userservice.entity.vo.SysSettingVO;
import com.easychat.user.userservice.entity.vo.UserInfoVO;
import com.easychat.user.userservice.mapper.UserInfoBeautyMapper;
import com.easychat.user.userservice.service.UserInfoBeautyService;
import com.easychat.user.userservice.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.tomcat.jni.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserInfoBeautyService userInfoBeautyService;

    @Autowired
    private RedisComponet redisComponet;

    @Autowired
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Autowired
    private AvatarConfig avatarConfig;

    @DubboReference(check = false)
    private AdminDubboService  adminDubboService;

    /**
     * 验证码
     */
    @ApiOperation("获取验证码")
    @GetMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_CHECK_CODE_KEY_TIME);
        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    /**
     * 注册
     * @param userFormDTO 用户信息DTO
     */
    @ApiOperation("注册")
    @PostMapping("/register")
    public ResponseVO register(@ModelAttribute UserFormDTO userFormDTO) {
        try {
            if (!userFormDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            userInfoService.register(userFormDTO);
            return getSuccessResponseVO(null);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey());
        }
    }

    /**
     * 登录接口
     */
    @ApiOperation("登录接口")
    @PostMapping("/login")
    public ResponseVO login(@ModelAttribute UserFormDTO userFormDTO) {
        log.info("登录请求参数: {}", userFormDTO);
        try {
            if (!userFormDTO.getCheckCode().equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey()))) {
                throw new BusinessException(Constants.ERROR_MSG_CHECK_CODE);
            }
            UserInfoVO userInfoVO = userInfoService.login(userFormDTO);
            log.info("登录成功{}", userInfoVO);
            return getSuccessResponseVO(userInfoVO);
        } finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + userFormDTO.getCheckCodeKey());
        }
    }

    /**
     * 获取系统设置
     * @return 系统设置
     */
    @ApiOperation("获取系统设置")
    @GetMapping("/setting")
    public ResponseVO getSysSetting() {
        SysSettingDTO sysSettingDTO = redisComponet.getSysSetting();
        SysSettingVO sysSettingVO = new SysSettingVO();
        BeanUtils.copyProperties(sysSettingDTO, sysSettingVO);
        return getSuccessResponseVO(sysSettingVO);
    }


    /**
     * 根据用户ID搜索用户信息
     * @param contactId 用户id
     * @return 用户信息
     */
    @ApiOperation("根据用户ID搜索用户信息")
    @GetMapping("/search/{contactId}")
    public ResponseVO searchUserInfo(@PathVariable("contactId") String contactId) {
        SearchResultVO searchResultVO = userInfoService.searchUserInfo(contactId);
        return getSuccessResponseVO(searchResultVO);
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("")
    public ResponseVO getUserInfo() {
        // 打印用户ID，用于调试
        String userId = UserContext.getUser();
        System.out.println("UserController getUserInfo - UserContext userId: " + userId);
        
        UserInfo userInfo = userInfoService.getById(userId);
        return getSuccessResponseVO(userInfo);
    }

    /**
     * 修改用户信息
     */
    @ApiOperation("修改用户信息")
    @PutMapping("")
    public ResponseVO updateUserInfo(@ModelAttribute UserInfoDTO userInfoDTO) {
        UserInfo userInfo = userInfoService.updateUserInfo(userInfoDTO);
        return getSuccessResponseVO(userInfo);
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PutMapping("/password")
    public ResponseVO updatePassword(@ModelAttribute UserInfoDTO userInfoDTO) {
        log.info("修改密码请求参数: {}", userInfoDTO);
        userInfoService.updatePassword(userInfoDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 退出登录
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public ResponseVO logout() {
        // TODO 关闭ws连接
        // 清除Redis中的用户登录信息
        redisComponet.cleanUserTokenByUserId(UserContext.getUser());
        return getSuccessResponseVO(null);
    }

    /**
     * 加载用户列表
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 用户列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/admin/userlist")
    public ResponseVO getUserList(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserInfo> page = new Page<>(pageNo, pageSize);
        IPage<UserInfo> userPage = userInfoService.page(page, Wrappers.emptyWrapper());
        PageResultVO pageResultVO = new PageResultVO();
        pageResultVO.setList(userPage.getRecords()); // 用户数据列表
        pageResultVO.setPageNo((int) userPage.getCurrent()); // 当前页码
        pageResultVO.setPageSize((int) userPage.getSize()); // 每页条数
        pageResultVO.setPageTotal((int) userPage.getPages()); // 总页数
        pageResultVO.setTotalCount((int) userPage.getTotal()); // 总记录数
        return getSuccessResponseVO(pageResultVO);
    }

    /**
     * 加载靓号列表
     */
    @ApiOperation("获取用户列表")
    @GetMapping("/admin/userBeautyList")
    public ResponseVO getUserBeautyList(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<UserInfoBeauty> page = new Page<>(pageNo, pageSize);
        IPage<UserInfoBeauty> userPage = userInfoBeautyMapper.selectPage(page, Wrappers.emptyWrapper());
        PageResultVO pageResultVO = new PageResultVO();
        pageResultVO.setList(userPage.getRecords()); // 用户数据列表
        pageResultVO.setPageNo((int) userPage.getCurrent()); // 当前页码
        pageResultVO.setPageSize((int) userPage.getSize()); // 每页条数
        pageResultVO.setPageTotal((int) userPage.getPages()); // 总页数
        pageResultVO.setTotalCount((int) userPage.getTotal()); // 总记录数
        return getSuccessResponseVO(pageResultVO);
    }

    /**
     * 新增或修改靓号
     */
    @ApiOperation("新增/修改靓号")
    @PostMapping("/admin/userBeauty")
    public ResponseVO addUserBeautyList(@ModelAttribute UserInfoBeautyDTO userInfoBeautyDTO) {
        UserInfoBeauty userInfoBeauty = new UserInfoBeauty();
        BeanUtils.copyProperties(userInfoBeautyDTO,userInfoBeauty);
        userInfoBeautyService.saveOrUpdate(userInfoBeauty);
        return getSuccessResponseVO(null);
    }



    /**
     * 删除靓号
     */
    @ApiOperation("删除靓号")
    @DeleteMapping("/admin/userBeauty/{id}")
    public ResponseVO deleteUserBeauty(@PathVariable("id") Integer id) {
        userInfoBeautyService.removeById(id);
        return getSuccessResponseVO(null);
    }

    /**
     * 版本更新
     */
     @ApiOperation("版本更新")
     @PostMapping("/update")
     public ResponseVO postUpdate(String appVersion) {
         log.info("<UNK>: {}", appVersion);
         if(appVersion==null||"".equals(appVersion)){
             throw new BusinessException("版本信息丢失");
         }
        AppUpdate appUpdate = adminDubboService.getUpdate(appVersion, UserContext.getUser());
        if (appUpdate == null) {
            return getSuccessResponseVO(null);
        }
        AppUpdateVO updateVO = new AppUpdateVO();
        BeanUtils.copyProperties(appUpdate, updateVO);
        // 获取当前项目根目录
        String projectPath = System.getProperty("user.dir");
        // 构建完整的文件夹路径
        String filePath = projectPath + File.separator + avatarConfig.getFilePath();
        File file = new File(filePath + File.separator + updateVO.getVersion() + avatarConfig.getFileSuffix());
        updateVO.setSize(file.length());
        updateVO.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));
        String fileName = updateVO.getVersion() + avatarConfig.getFileSuffix();
        updateVO.setFileName(fileName);
        return getSuccessResponseVO(appUpdate);
     }


    /**
     * 获取用户信息(供contact服务使用)
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/service/{userId}")
    public UserInfo ServiceGetUserInfo(@PathVariable("userId") String userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        return userInfo;
    }
}