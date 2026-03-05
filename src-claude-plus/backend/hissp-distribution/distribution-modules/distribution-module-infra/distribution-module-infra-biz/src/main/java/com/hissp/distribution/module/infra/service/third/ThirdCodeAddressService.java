package com.hissp.distribution.module.infra.service.third;

import com.hissp.distribution.module.infra.dal.dataobject.third.ThirdCodeAddressDO;

import java.util.List;

public interface ThirdCodeAddressService {
    List<ThirdCodeAddressDO> listProvinces(String provider);
    List<ThirdCodeAddressDO> listCities(String provider, String parentCode);
    List<ThirdCodeAddressDO> listDistricts(String provider, String parentCode);
    int importFromExcel(String provider, String version, String filePath);

    /**
     * 从上传流导入（适用于网页上传）
     */
    int importFromExcel(String provider, String version, java.io.InputStream inputStream);
}
