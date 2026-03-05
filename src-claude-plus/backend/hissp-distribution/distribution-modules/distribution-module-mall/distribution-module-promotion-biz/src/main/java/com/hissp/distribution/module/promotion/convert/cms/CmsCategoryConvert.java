package com.hissp.distribution.module.promotion.convert.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategoryRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategorySaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsCategoryRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * CMS分类 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsCategoryConvert {

    CmsCategoryConvert INSTANCE = Mappers.getMapper(CmsCategoryConvert.class);

    CmsCategoryDO convert(CmsCategorySaveReqVO bean);

    CmsCategoryRespVO convert(CmsCategoryDO bean);

    List<CmsCategoryRespVO> convertList(List<CmsCategoryDO> list);

    PageResult<CmsCategoryRespVO> convertPage(PageResult<CmsCategoryDO> page);

    AppCmsCategoryRespVO convertApp(CmsCategoryDO bean);

    List<AppCmsCategoryRespVO> convertAppList(List<CmsCategoryDO> list);

}
