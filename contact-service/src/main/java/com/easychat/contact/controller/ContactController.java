package com.easychat.contact.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.entity.dto.ContactApplyDTO;
import com.easychat.contact.entity.dto.ContactDTO;
import com.easychat.contact.entity.po.Contact;
import com.easychat.contact.entity.po.ContactApply;
import com.easychat.contact.entity.vo.PageResultVO;
import com.easychat.contact.entity.vo.SearchResultVO;
import com.easychat.contact.service.ContactApplyService;
import com.easychat.contact.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController extends BaseController {

    @Autowired
    private ContactService contactService;
    @Autowired
    private ContactApplyService contactApplyService;

    /**
     * 搜索群组/用户
     * @param contactId 群组/用户ID
     * @return 搜索结果
     */
    @PostMapping("/search")
    public ResponseVO search(@RequestParam("contactId") String contactId) {
        SearchResultVO searchResultVO= contactService.search(contactId);
        return getSuccessResponseVO(searchResultVO);
    }

    /**
     * 申请添加好友/加入群聊
     * @param contactApplyDTO 好友申请信息/群聊申请信息
     */
    @PostMapping(value = "/applyAdd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO applyAdd(@ModelAttribute ContactApplyDTO contactApplyDTO) {
        contactApplyDTO.setApplyUserId(UserContext.getUser());
        contactApplyDTO.setApplyInfo(contactApplyDTO.getApplyInfo() == null ? "我是"+ UserContext.getNickName() : contactApplyDTO.getApplyInfo());
        contactService.applyAdd(contactApplyDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载好友申请/群聊申请
     * @param pageNo 页面号
     * @return 结果
     */
    @PostMapping("/loadApply")
    public ResponseVO loadApply(@RequestParam("pageNo") Integer pageNo) {
        PageResultVO<ContactApply> pageResultVO = new PageResultVO<>();
        IPage<ContactApply> page = contactApplyService.page(
                new Page<>(pageNo, 10),
                new LambdaQueryWrapper<ContactApply>()
                        .eq(ContactApply::getReceiveUserId, UserContext.getUser())
        );
        pageResultVO.setPageSize(10);
        pageResultVO.setPageTotal((int) page.getTotal());
        pageResultVO.setList(page.getRecords());
        pageResultVO.setTotalCount((int)page.getTotal());
        pageResultVO.setPageNo((int)page.getCurrent());
        // TODO 联合查询用户昵称或者群聊昵称

        return  getSuccessResponseVO(pageResultVO);

    }

    /**
     * 处理申请
     * @param applyId 申请ID
     * @param status 处理操作 0:拒绝 1:同意 2:拉黑
     */
    @PostMapping("/dealWithApply")
    public ResponseVO dealWithApply(@NotNull Integer applyId, @NotNull Integer status) {
        contactApplyService.dealWithApply(applyId, status);
        return getSuccessResponseVO(null);
    }


    /**
     * 获取联系人列表
     */
    @PostMapping("/loadContact")
    public ResponseVO loadContact(@NotEmpty String contactType) {
        List<Contact> contacts = contactService.getContactList(contactType);
        return getSuccessResponseVO(contacts);
    }


    /**
     * 获取联系人详情
     * @param contactId 联系人ID
     * @return 联系人详情
     */
//    @PostMapping("/loadContactDetail")
//    public ResponseVO loadContactDetail(@NotEmpty String contactId) {
//        Contact userContact = contactService.loadContactDetail(contactId);
//        return getSuccessResponseVO(userContact);
//    }

    /**
     * 创建关系
     */
    @PostMapping("/createContact")
    public ResponseVO createContact(@ModelAttribute ContactDTO contactDTO) {
        contactService.createContact(contactDTO);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除联系人
     */


    /**
     * 拉黑联系人
     */

}
