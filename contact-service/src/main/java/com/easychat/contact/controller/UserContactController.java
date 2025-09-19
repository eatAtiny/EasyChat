package com.easychat.contact.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.entity.dto.UserContactApplyDTO;
import com.easychat.contact.entity.po.UserContactApply;
import com.easychat.contact.entity.vo.PageResultVO;
import com.easychat.contact.entity.vo.SearchResultVO;
import com.easychat.contact.mapper.UserContactApplyMapper;
import com.easychat.contact.service.UserContactApplyService;
import com.easychat.contact.service.UserContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class UserContactController extends BaseController {

    @Autowired
    private UserContactService userContactService;

    @Autowired
    private UserContactApplyService userContactApplyService;


    @PostMapping("/search")
    public ResponseVO search(@RequestParam("contactId") String contactId) {
        SearchResultVO searchResultVO= userContactService.search(contactId);
        return getSuccessResponseVO(searchResultVO);
    }

    @PostMapping(value = "/applyAdd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO applyAdd(@ModelAttribute UserContactApplyDTO userContactApplyDTO) {
        userContactService.applyAdd(userContactApplyDTO);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/loadApply")
    public ResponseVO loadApply(@RequestParam("pageNo") Integer pageNo) {
        PageResultVO<UserContactApply> pageResultVO = new PageResultVO<>();
        IPage<UserContactApply> page = userContactApplyService.page(
                new Page<>(pageNo, 10),
                new LambdaQueryWrapper<UserContactApply>()
                        .eq(UserContactApply::getReceiveUserId, UserContext.getUser())
        );
        pageResultVO.setPageSize(10);
        pageResultVO.setPageTotal((int) page.getTotal());
        pageResultVO.setList(page.getRecords());
        pageResultVO.setTotalCount((int)page.getTotal());
        pageResultVO.setPageNo((int)page.getCurrent());
        // TODO 联合查询用户昵称或者群聊昵称

        return  getSuccessResponseVO(pageResultVO);

    }

}
