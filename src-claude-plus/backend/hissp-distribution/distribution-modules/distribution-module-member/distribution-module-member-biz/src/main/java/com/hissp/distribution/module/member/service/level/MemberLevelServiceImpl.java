package com.hissp.distribution.module.member.service.level;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.member.enums.ErrorCodeConstants.*;

import java.time.LocalDateTime;
import java.util.*;

import com.hissp.distribution.module.mb.message.MemberLevelChangeMessage;
import com.hissp.distribution.module.member.event.MemberLevelChangeEvent;
import com.hissp.distribution.module.member.mq.producer.MemberEventProducer;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.google.common.annotations.VisibleForTesting;
import com.hissp.distribution.module.member.controller.admin.level.vo.level.MemberLevelCreateReqVO;
import com.hissp.distribution.module.member.controller.admin.level.vo.level.MemberLevelListReqVO;
import com.hissp.distribution.module.member.controller.admin.level.vo.level.MemberLevelUpdateReqVO;
import com.hissp.distribution.module.member.controller.admin.user.vo.MemberUserUpdateLevelReqVO;
import com.hissp.distribution.module.member.convert.level.MemberLevelConvert;
import com.hissp.distribution.module.member.convert.level.MemberLevelRecordConvert;
import com.hissp.distribution.module.member.dal.dataobject.level.MemberLevelDO;
import com.hissp.distribution.module.member.dal.dataobject.level.MemberLevelRecordDO;
import com.hissp.distribution.module.member.dal.dataobject.user.MemberUserDO;
import com.hissp.distribution.module.member.dal.mysql.level.MemberLevelMapper;
import com.hissp.distribution.module.member.enums.MemberExperienceBizTypeEnum;
import com.hissp.distribution.module.member.service.user.MemberUserService;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 会员等级 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class MemberLevelServiceImpl implements MemberLevelService {

    @Resource
    private MemberLevelMapper memberLevelMapper;

    @Resource
    private MemberLevelRecordService memberLevelRecordService;
    @Resource
    private MemberExperienceRecordService memberExperienceRecordService;
    @Resource
    private MemberUserService memberUserService;

    @Resource
    private MemberEventProducer memberEventProducer;
    @Autowired
    private BrokerageUserApi brokerageUserApi;

    @Override
    public Long createLevel(MemberLevelCreateReqVO createReqVO) {
        // 校验配置是否有效
        validateConfigValid(null, createReqVO.getName(), createReqVO.getLevel(), createReqVO.getExperience());

        // 插入
        MemberLevelDO level = MemberLevelConvert.INSTANCE.convert(createReqVO);
        memberLevelMapper.insert(level);
        // 返回
        return level.getId();
    }

    @Override
    public void updateLevel(MemberLevelUpdateReqVO updateReqVO) {
        // 校验存在
        validateLevelExists(updateReqVO.getId());
        // 校验配置是否有效
        validateConfigValid(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getLevel(),
            updateReqVO.getExperience());

        // 更新
        MemberLevelDO updateObj = MemberLevelConvert.INSTANCE.convert(updateReqVO);
        memberLevelMapper.updateById(updateObj);
    }

    @Override
    public void deleteLevel(Long id) {
        // 校验存在
        validateLevelExists(id);
        // 校验分组下是否有用户
        validateLevelHasUser(id);
        // 删除
        memberLevelMapper.deleteById(id);
    }

    @VisibleForTesting
    MemberLevelDO validateLevelExists(Long id) {
        MemberLevelDO levelDO = memberLevelMapper.selectById(id);
        if (levelDO == null) {
            throw exception(LEVEL_NOT_EXISTS);
        }
        return levelDO;
    }

    @VisibleForTesting
    void validateNameUnique(List<MemberLevelDO> list, Long id, String name) {
        for (MemberLevelDO levelDO : list) {
            if (ObjUtil.notEqual(levelDO.getName(), name)) {
                continue;
            }
            if (id == null || !id.equals(levelDO.getId())) {
                throw exception(LEVEL_NAME_EXISTS, levelDO.getName());
            }
        }
    }

    @VisibleForTesting
    void validateLevelUnique(List<MemberLevelDO> list, Long id, Integer level) {
        for (MemberLevelDO levelDO : list) {
            if (ObjUtil.notEqual(levelDO.getLevel(), level)) {
                continue;
            }

            if (id == null || !id.equals(levelDO.getId())) {
                throw exception(LEVEL_VALUE_EXISTS, levelDO.getLevel(), levelDO.getName());
            }
        }
    }

    @VisibleForTesting
    void validateExperienceOutRange(List<MemberLevelDO> list, Long id, Integer level, Integer experience) {
        for (MemberLevelDO levelDO : list) {
            if (levelDO.getId().equals(id)) {
                continue;
            }

            if (levelDO.getLevel() < level) {
                // 经验大于前一个等级
                if (experience <= levelDO.getExperience()) {
                    throw exception(LEVEL_EXPERIENCE_MIN, levelDO.getName(), levelDO.getExperience());
                }
            } else if (levelDO.getLevel() > level) {
                // 小于下一个级别
                if (experience >= levelDO.getExperience()) {
                    throw exception(LEVEL_EXPERIENCE_MAX, levelDO.getName(), levelDO.getExperience());
                }
            }
        }
    }

    @VisibleForTesting
    void validateConfigValid(Long id, String name, Integer level, Integer experience) {
        List<MemberLevelDO> list = memberLevelMapper.selectList();
        // 校验名称唯一
        validateNameUnique(list, id, name);
        // 校验等级唯一
        validateLevelUnique(list, id, level);
        // 校验升级所需经验是否有效: 大于前一个等级，小于下一个级别
        validateExperienceOutRange(list, id, level, experience);
    }

    @VisibleForTesting
    void validateLevelHasUser(Long id) {
        Long count = memberUserService.getUserCountByLevelId(id);
        if (count > 0) {
            throw exception(LEVEL_HAS_USER);
        }
    }

    @Override
    public MemberLevelDO getLevel(Long id) {
        return id != null && id > 0 ? memberLevelMapper.selectById(id) : null;
    }

    @Override
    public List<MemberLevelDO> getLevelList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return memberLevelMapper.selectBatchIds(ids);
    }

    @Override
    public List<MemberLevelDO> getLevelList(MemberLevelListReqVO listReqVO) {
        return memberLevelMapper.selectList(listReqVO);
    }

    @Override
    public List<MemberLevelDO> getLevelListByStatus(Integer status) {
        return memberLevelMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserLevel(MemberUserUpdateLevelReqVO updateReqVO) {
        // pre 登记变化驱动更新的校验
        MemberUserDO user = memberUserService.getUser(updateReqVO.getId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        // 等级未发生变化
        if (ObjUtil.equal(user.getLevelId(), updateReqVO.getLevelId())) {
            return;
        }

        // 1. 记录等级变动
        MemberLevelRecordDO levelRecord =
            new MemberLevelRecordDO().setUserId(user.getId()).setRemark(updateReqVO.getReason());

        MemberLevelDO afterLevel = null;
        if (updateReqVO.getLevelId() == null) {
            // 取消用户等级时，需要扣减经验
            levelRecord.setExperience(-user.getExperience());
            levelRecord.setUserExperience(0);
            levelRecord.setDescription("等级取消");
        } else {
            // 复制等级配置
            afterLevel = validateLevelExists(updateReqVO.getLevelId());
            MemberLevelRecordConvert.INSTANCE.copyTo(afterLevel, levelRecord);
            // 变动经验值 = 等级的升级经验 - 会员当前的经验；正数为增加经验，负数为扣减经验
            levelRecord.setExperience(afterLevel.getExperience() - user.getExperience());
            levelRecord.setUserExperience(afterLevel.getExperience()); // 会员当前的经验 = 等级的升级经验
            levelRecord.setDescription("等级调整为：" + afterLevel.getName());
        }
        memberLevelRecordService.createLevelRecord(levelRecord);

        // 2. 记录会员经验变动
        memberExperienceRecordService.createExperienceRecord(user.getId(), levelRecord.getExperience(),
            levelRecord.getUserExperience(), MemberExperienceBizTypeEnum.ADMIN,
            String.valueOf(MemberExperienceBizTypeEnum.ADMIN.getType()));

        // 3. 更新会员表上的等级编号、经验值
        memberUserService.updateUserLevel(user.getId(), updateReqVO.getLevelId(), levelRecord.getUserExperience());

    }

    @Override
    public void updateUserLevelWithMb(MemberUserUpdateLevelReqVO updateReqVO) {
        // 获取用户之前等级信息，用于后续处理
        MemberUserDO beforeUser = memberUserService.getUser(updateReqVO.getId());
        // 调用基础方法更新用户等级
        updateUserLevel(updateReqVO);
        // 同步等级更新逻辑，主要是给分销体系
        syncManualLevelChangeLogic(beforeUser, updateReqVO);
    }

    private void syncManualLevelChangeLogic(MemberUserDO beforeUser, MemberUserUpdateLevelReqVO updateReqVO) {
        Long beforeLevelId = beforeUser.getLevelId();
        Long afterLevelId = updateReqVO.getLevelId();
        // 如果前后等级没有变化，则不处理
        if (Objects.equals(beforeLevelId, afterLevelId)) {
            return;
        }

        Integer beforeLevel = null;
        if (beforeLevelId != null) {
            MemberLevelDO level = getLevel(beforeLevelId);
            beforeLevel = level != null ? level.getLevel() : null;
        }
        Integer afterLevel = null;
        if (afterLevelId != null) {
            MemberLevelDO level = getLevel(afterLevelId);
            afterLevel = level != null ? level.getLevel() : null;
        }
        // 发送MQ消息替代Spring Event
        MemberLevelChangeMessage message = new MemberLevelChangeMessage();
        message.setUserId(updateReqVO.getId())
        	.setBeforeLevelId(beforeLevelId)
        	.setBeforeLevel(beforeLevel)
        	.setAfterLevelId(afterLevelId)
        	.setAfterLevel(afterLevel)
        	.setManualEnableBrokerage(updateReqVO.getEnableBrokerage())
            .setPackageId(updateReqVO.getPackageId())
        	.setOperatorId(getLoginUserId())
        	.setOperateTime(LocalDateTime.now());
        memberEventProducer.sendMemberLevelChangeMessage(message);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId) {
        if (experience == 0) {
            return;
        }
        if (!bizType.isAdd() && experience > 0) {
            experience = -experience;
        }

        // 1. 创建经验记录
        MemberUserDO user = memberUserService.getUser(userId);
        Integer userExperience = ObjUtil.defaultIfNull(user.getExperience(), 0);
        userExperience = NumberUtil.max(userExperience + experience, 0); // 防止扣出负数
        MemberLevelRecordDO levelRecord = new MemberLevelRecordDO().setUserId(user.getId()).setExperience(experience)
            .setUserExperience(userExperience).setLevelId(user.getLevelId());
        memberExperienceRecordService.createExperienceRecord(userId, experience, userExperience, bizType, bizId);

        // 2.1 保存等级变更记录
        MemberLevelDO newLevel = calculateNewLevel(user, userExperience);
        if (newLevel != null) {
            MemberLevelRecordConvert.INSTANCE.copyTo(newLevel, levelRecord);
            memberLevelRecordService.createLevelRecord(levelRecord);

            // 2.2 给会员发送等级变动消息
            notifyMemberLevelChange(userId, newLevel);
        }

        // 3. 更新会员表上的等级编号、经验值
        memberUserService.updateUserLevel(user.getId(), levelRecord.getLevelId(), userExperience);
    }

    /**
     * 计算会员等级
     *
     * @param user 会员
     * @param userExperience 会员当前的经验值
     * @return 会员新的等级，null表示无变化
     */
    private MemberLevelDO calculateNewLevel(MemberUserDO user, int userExperience) {
        List<MemberLevelDO> list = getEnableLevelList();
        if (CollUtil.isEmpty(list)) {
            log.warn("计算会员等级失败：会员等级配置不存在");
            return null;
        }

        MemberLevelDO matchLevel = list.stream().filter(level -> userExperience >= level.getExperience())
            .max(Comparator.nullsFirst(Comparator.comparing(MemberLevelDO::getLevel))).orElse(null);
        if (matchLevel == null) {
            log.warn("计算会员等级失败：未找到会员{}经验{}对应的等级配置", user.getId(), userExperience);
            return null;
        }

        // 等级没有变化
        if (ObjectUtil.equal(matchLevel.getId(), user.getLevelId())) {
            return null;
        }

        return matchLevel;
    }

    private void notifyMemberLevelChange(Long userId, MemberLevelDO level) {
        // todo: 给会员发消息
    }

}
