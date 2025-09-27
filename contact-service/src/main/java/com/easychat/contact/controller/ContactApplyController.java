package com.easychat.contact.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.entity.dto.ContactApplyDTO;
import com.easychat.contact.entity.po.ContactApply;
import com.easychat.contact.entity.vo.PageResultVO;
import com.easychat.contact.service.ContactApplyService;
import com.easychat.contact.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/contact/apply")
public class ContactApplyController extends BaseController {

    @Autowired
    private ContactApplyService contactApplyService;

    /**
     * 申请添加好友/加入群聊
     * @param contactApplyDTO 好友申请信息/群聊申请信息
     */
    @PostMapping("")
    public ResponseVO applyAdd(ContactApplyDTO contactApplyDTO) {
        contactApplyDTO.setApplyUserId(UserContext.getUser());
        contactApplyDTO.setApplyInfo(contactApplyDTO.getApplyInfo() == null ? "我是"+ UserContext.getNickName() : contactApplyDTO.getApplyInfo());
        contactApplyService.applyAdd(contactApplyDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载好友申请/群聊申请
     * @param pageNo 页面号
     * @return 结果
     */
    @GetMapping("")
    public ResponseVO getApplyList(@RequestParam("pageNo") Integer pageNo) {
        PageResultVO<ContactApply> pageResultVO = new PageResultVO<>();
        IPage<ContactApply> page = contactApplyService.getApplyList(pageNo);
        pageResultVO.setPageSize(10);
        pageResultVO.setPageTotal((int) page.getTotal());
        pageResultVO.setList(page.getRecords());
        pageResultVO.setTotalCount((int)page.getTotal());
        pageResultVO.setPageNo((int)page.getCurrent());
        return  getSuccessResponseVO(pageResultVO);

    }

    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 0:拒绝 1:同意 2:拉黑
     */
    @PostMapping("/deal")
    public ResponseVO dealWithApply(@NotNull Integer applyId, @NotNull Integer status) {
        contactApplyService.dealWithApply(applyId, status);
        return getSuccessResponseVO(null);
    }
}