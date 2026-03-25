package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualCreditsTransaction;

/**
 * 积分交易流水Mapper接口
 */
public interface VirtualCreditsTransactionMapper {
    public VirtualCreditsTransaction selectByTxId(Long txId);

    public List<VirtualCreditsTransaction> selectByUserId(Long userId);

    public List<VirtualCreditsTransaction> selectVirtualCreditsTransactionList(VirtualCreditsTransaction tx);

    public int insertVirtualCreditsTransaction(VirtualCreditsTransaction tx);

    /** 管理后台积分流水查询（支持 direction、时间范围过滤） */
    List<VirtualCreditsTransaction> selectAdminCreditsTransactionList(VirtualCreditsTransaction tx);
}
