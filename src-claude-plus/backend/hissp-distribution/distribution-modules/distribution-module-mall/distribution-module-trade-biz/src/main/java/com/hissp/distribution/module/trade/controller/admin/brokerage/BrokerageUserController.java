package com.hissp.distribution.module.trade.controller.admin.brokerage;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.*;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageUserConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageUserService;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageWithdrawService;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMap;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static java.util.Arrays.asList;

@Tag(name = "管理后台 - 分销用户")
@RestController
@RequestMapping("/trade/brokerage-user")
@Validated
public class BrokerageUserController {

    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private BrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;

    @PostMapping("/create")
    @Operation(summary = "创建分销用户")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:create')")
    public CommonResult<Long> createBrokerageUser(@Valid @RequestBody BrokerageUserCreateReqVO createReqVO) {
        return success(brokerageUserService.createBrokerageUser(createReqVO));
    }

    @PutMapping("/update-bind-user")
    @Operation(summary = "修改推广员")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:update-bind-user')")
    public CommonResult<Boolean> updateBindUser(@Valid @RequestBody BrokerageUserUpdateBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), updateReqVO.getBindUserId());
        return success(true);
    }

    @PutMapping("/clear-bind-user")
    @Operation(summary = "清除推广员")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:clear-bind-user')")
    public CommonResult<Boolean> clearBindUser(@Valid @RequestBody BrokerageUserClearBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), null);
        return success(true);
    }

    @PutMapping("/update-brokerage-enable")
    @Operation(summary = "修改推广资格")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:update-brokerage-enable')")
    public CommonResult<Boolean> updateBrokerageEnabled(@Valid @RequestBody BrokerageUserUpdateBrokerageEnabledReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserEnabled(updateReqVO.getId(), updateReqVO.getEnabled());
        return success(true);
    }
    
    @PutMapping("/update-level")
    @Operation(summary = "修改用户等级")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:update-level')")
    public CommonResult<Boolean> updateUserLevel(@Valid @RequestBody BrokerageUserUpdateLevelReqVO updateReqVO) {
        brokerageUserService.updateUserLevel(updateReqVO.getId(), updateReqVO.getLevelId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得分销用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<BrokerageUserRespVO> getBrokerageUser(@RequestParam("id") Long id) {
        BrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(id);
        // TODO @疯狂：是不是搞成一个统一的 convert？
        BrokerageUserRespVO respVO = BrokerageUserConvert.INSTANCE.convert(brokerageUser);
        return success(BrokerageUserConvert.INSTANCE.copyTo(memberUserApi.getUser(id), respVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得分销用户分页")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<PageResult<BrokerageUserRespVO>> getBrokerageUserPage(@Valid BrokerageUserPageReqVO pageVO) {
        // 分页查询
        PageResult<BrokerageUserDO> pageResult = brokerageUserService.getBrokerageUserPage(pageVO);

        // 查询用户信息
        Set<Long> userIds = convertSet(pageResult.getList(), BrokerageUserDO::getId);
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        // 查询上级用户信息
        Set<Long> bindUserIds = convertSet(pageResult.getList(), BrokerageUserDO::getBindUserId);
        bindUserIds.removeIf(id -> id == null); // 移除null值
        Map<Long, MemberUserRespDTO> bindUserMap = memberUserApi.getUserMap(bindUserIds);

        // 查询等级信息 - 从分佣用户表和用户表中获取等级ID
        Set<Long> levelIds = convertSet(userMap.values(), MemberUserRespDTO::getLevelId);
        // 同时收集分佣用户表中的等级ID
        Set<Long> brokerageLevelIds = convertSet(pageResult.getList(), BrokerageUserDO::getLevelId);
        levelIds.addAll(brokerageLevelIds);
        levelIds.removeIf(id -> id == null); // 移除null值
        Map<Long, MemberLevelRespDTO> levelMap = memberLevelApi.getMemberLevelMap(levelIds);
        Map<Long, String> levelNameMap = convertMap(levelMap.values(), MemberLevelRespDTO::getId, MemberLevelRespDTO::getName);

        // 合计分佣的推广订单
        Map<Long, UserBrokerageSummaryRespBO> brokerageOrderSummaryMap = brokerageRecordService.getUserBrokerageSummaryMapByUserId(
                userIds, BrokerageRecordBizTypeEnum.ORDER_ADD.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        // 合计分佣的推广用户
        // TODO @疯狂：转成 map 批量读取
        Map<Long, Long> brokerageUserCountMap = convertMap(userIds,
                userId -> userId,
                userId -> brokerageUserService.getBrokerageUserCountByBindUserId(userId, null));
        // 合计分佣的提现
        // TODO @疯狂：如果未来支持了打款这个动作，可能 status 会不对；
        Map<Long, BrokerageWithdrawSummaryRespBO> withdrawMap = brokerageWithdrawService.getWithdrawSummaryMapByUserId(
                userIds, asList(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS, BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS,BrokerageWithdrawStatusEnum.WITHDRAW_SUBMIT_SUCCESS));
        // 拼接返回
        return success(BrokerageUserConvert.INSTANCE.convertPage(pageResult, userMap, brokerageUserCountMap,
                brokerageOrderSummaryMap, withdrawMap, bindUserMap, levelNameMap));
    }
    
    @GetMapping("/list-by-level")
    @Operation(summary = "获得指定等级的分销用户列表")
    @Parameter(name = "levelId", description = "等级编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<List<BrokerageUserRespVO>> getBrokerageUserListByLevelId(@RequestParam("levelId") Long levelId) {
        return success(BrokerageUserConvert.INSTANCE.convertList(brokerageUserService.getBrokerageUserListByLevelId(levelId)));
    }

    @GetMapping("/team-graph/{userId}")
    @Operation(summary = "获得分销团队关系图谱")
    @Parameter(name = "userId", description = "用户编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<TeamGraphVO> getTeamGraph(@PathVariable("userId") Long userId) {
        return success(brokerageUserService.getTeamGraph(userId));
    }

}
