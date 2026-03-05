package com.hissp.distribution.module.infra.dal.dataobject.third;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

/**
 * 第三方地址码表 DO（首信易）
 */
@TableName(value = "infra_third_code_address", autoResultMap = true)
@KeySequence("infra_third_code_address_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdCodeAddressDO extends TenantBaseDO {

    private Long id;
    private String provider; // 三方提供方，固定 PAYEASE
    private String code;     // 三方地址编码
    private String name;     // 名称
    private Integer level;   // 层级：1-省，2-市，3-区县
    private String parentCode; // 父级编码
    private Integer sort;    // 排序
    private Integer status;  // 状态：0-启用，1-禁用
    private String version;  // 版本号
    private String remark;   // 备注
}

