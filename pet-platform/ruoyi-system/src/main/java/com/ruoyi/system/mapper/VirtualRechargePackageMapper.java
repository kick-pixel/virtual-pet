package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualRechargePackage;

/**
 * 充值套餐Mapper接口
 */
public interface VirtualRechargePackageMapper {
    public VirtualRechargePackage selectByPackageId(Long packageId);

    public List<VirtualRechargePackage> selectActivePackages();

    public List<VirtualRechargePackage> selectVirtualRechargePackageList(VirtualRechargePackage pkg);

    public int insertVirtualRechargePackage(VirtualRechargePackage pkg);

    public int updateVirtualRechargePackage(VirtualRechargePackage pkg);
}
