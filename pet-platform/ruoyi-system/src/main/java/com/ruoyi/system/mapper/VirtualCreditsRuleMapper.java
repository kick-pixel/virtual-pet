package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualCreditsRule;

/**
 * 积分规则Mapper接口
 */
public interface VirtualCreditsRuleMapper {
    public VirtualCreditsRule selectByRuleId(Long ruleId);

    public VirtualCreditsRule selectByRuleCode(String ruleCode);

    public List<VirtualCreditsRule> selectVirtualCreditsRuleList(VirtualCreditsRule rule);

    public List<VirtualCreditsRule> selectActiveRules();

    public int insertVirtualCreditsRule(VirtualCreditsRule rule);

    public int updateVirtualCreditsRule(VirtualCreditsRule rule);
}
