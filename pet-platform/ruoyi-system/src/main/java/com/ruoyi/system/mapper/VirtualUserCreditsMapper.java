package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.VirtualUserCredits;
import org.apache.ibatis.annotations.Param;

/**
 * 用户积分Mapper接口
 */
public interface VirtualUserCreditsMapper {
    public VirtualUserCredits selectByUserId(Long userId);

    public int insertVirtualUserCredits(VirtualUserCredits credits);

    public int updateVirtualUserCredits(VirtualUserCredits credits);

    /** 乐观锁冻结积分 */
    public int freezeCredits(@Param("userId") Long userId, @Param("amount") Long amount,
            @Param("version") Integer version);

    /** 确认消费（从冻结中扣减） */
    public int confirmSpend(@Param("userId") Long userId, @Param("amount") Long amount,
            @Param("version") Integer version);

    /** 退还冻结积分 */
    public int refundFrozen(@Param("userId") Long userId, @Param("amount") Long amount,
            @Param("version") Integer version);

    /** 直接增加积分（充值/奖励） */
    public int addCredits(@Param("userId") Long userId, @Param("amount") Long amount,
            @Param("version") Integer version);
}
