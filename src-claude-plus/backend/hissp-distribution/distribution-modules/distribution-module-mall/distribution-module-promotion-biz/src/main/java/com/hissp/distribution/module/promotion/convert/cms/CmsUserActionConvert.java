package com.hissp.distribution.module.promotion.convert.cms;

import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsUserActionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * CMS用户行为 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsUserActionConvert {

    CmsUserActionConvert INSTANCE = Mappers.getMapper(CmsUserActionConvert.class);

    CmsUserActionDO convert(Long userId, Long articleId, String actionType);
    List<Long> convertToArticleIdList(List<CmsUserActionDO> list);

    // 添加这个方法来解决映射问题
    default Long convertToArticleId(CmsUserActionDO cmsUserActionDO) {
        if (cmsUserActionDO == null) {
            return null;
        }
        return cmsUserActionDO.getArticleId();
    }
}
