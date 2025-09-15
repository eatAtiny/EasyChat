package com.easychat.common.advice;



import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.entity.enums.ResponseCodeEnum;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


public class BaseController {


    protected static final String COOKIE_KEY_TOKEN = "token";

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";


    @Resource
    private RedisUtils redisUtils;


    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected TokenUserInfoDTO getTokenUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        TokenUserInfoDTO tokenUserInfoDto = (TokenUserInfoDTO) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

    protected void resetTokenUserInfo(HttpServletRequest request, TokenUserInfoDTO tokenUserInfoDto) {
        String token = request.getHeader("token");
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + token, tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }
}
