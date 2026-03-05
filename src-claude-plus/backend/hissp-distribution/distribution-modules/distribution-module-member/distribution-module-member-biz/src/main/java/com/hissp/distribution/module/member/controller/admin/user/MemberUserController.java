package com.hissp.distribution.module.member.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.controller.admin.user.vo.*;
import com.hissp.distribution.module.member.convert.user.MemberUserConvert;
import com.hissp.distribution.module.member.dal.dataobject.group.MemberGroupDO;
import com.hissp.distribution.module.member.dal.dataobject.level.MemberLevelDO;
import com.hissp.distribution.module.member.dal.dataobject.tag.MemberTagDO;
import com.hissp.distribution.module.member.dal.dataobject.user.MemberUserDO;
import com.hissp.distribution.module.member.enums.point.MemberPointBizTypeEnum;
import com.hissp.distribution.module.member.service.group.MemberGroupService;
import com.hissp.distribution.module.member.service.level.MemberLevelService;
import com.hissp.distribution.module.member.service.point.MemberPointRecordService;
import com.hissp.distribution.module.member.service.tag.MemberTagService;
import com.hissp.distribution.module.member.service.user.MemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 会员用户")
@RestController
@RequestMapping("/member/user")
@Validated
public class MemberUserController {

    @Resource
    private MemberUserService memberUserService;
    @Resource
    private MemberTagService memberTagService;
    @Resource
    private MemberLevelService memberLevelService;
    @Resource
    private MemberGroupService memberGroupService;
    @Resource
    private MemberPointRecordService memberPointRecordService;

    @PutMapping("/update")
    @Operation(summary = "更新会员用户")
    @PreAuthorize("@ss.hasPermission('member:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody MemberUserUpdateReqVO updateReqVO) {
        memberUserService.updateUser(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-level")
    @Operation(summary = "更新会员用户等级")
    @PreAuthorize("@ss.hasPermission('member:user:update-level')")
    public CommonResult<Boolean> updateUserLevel(@Valid @RequestBody MemberUserUpdateLevelReqVO updateReqVO) {
        //获取before
        memberLevelService.updateUserLevelWithMb(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-point")
    @Operation(summary = "更新会员用户积分")
    @PreAuthorize("@ss.hasPermission('member:user:update-point')")
    public CommonResult<Boolean> updateUserPoint(@Valid @RequestBody MemberUserUpdatePointReqVO updateReqVO) {
        memberPointRecordService.createPointRecord(updateReqVO.getId(), updateReqVO.getPoint(),
                MemberPointBizTypeEnum.ADMIN, String.valueOf(getLoginUserId()));
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<MemberUserRespVO> getUser(@RequestParam("id") Long id) {
        MemberUserDO user = memberUserService.getUser(id);
        // 转换
        MemberUserRespVO userVO = MemberUserConvert.INSTANCE.convert03(user);
        
        // 填充会员等级名称
        if (user.getLevelId() != null) {
            MemberLevelDO level = memberLevelService.getLevel(user.getLevelId());
            if (level != null) {
                userVO.setLevelName(level.getName());
            }
        }
        
        return success(userVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员用户分页")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<PageResult<MemberUserRespVO>> getUserPage(@Valid MemberUserPageReqVO pageVO) {
        PageResult<MemberUserDO> pageResult = memberUserService.getUserPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 处理用户标签返显
        Set<Long> tagIds = pageResult.getList().stream()
                .map(MemberUserDO::getTagIds)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<MemberTagDO> tags = memberTagService.getTagList(tagIds);
        // 处理用户级别返显
        List<MemberLevelDO> levels = memberLevelService.getLevelList(
                convertSet(pageResult.getList(), MemberUserDO::getLevelId));
        // 处理用户分组返显
        List<MemberGroupDO> groups = memberGroupService.getGroupList(
                convertSet(pageResult.getList(), MemberUserDO::getGroupId));
        return success(MemberUserConvert.INSTANCE.convertPage(pageResult, tags, levels, groups));
    }

    @GetMapping("/list-by-nickname")
    @Operation(summary = "根据昵称模糊查询用户列表")
    @Parameter(name = "nickname", description = "用户昵称", required = true, example = "张三")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<List<MemberUserRespVO>> getUserListByNickname(@RequestParam("nickname") String nickname) {
        List<MemberUserDO> users = memberUserService.getUserListByNickname(nickname);
        return success(MemberUserConvert.INSTANCE.convertList03(users));
    }

}
