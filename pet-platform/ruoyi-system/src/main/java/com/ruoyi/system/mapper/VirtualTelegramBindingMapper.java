package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.VirtualTelegramBinding;

/**
 * Telegram用户绑定Mapper接口
 */
public interface VirtualTelegramBindingMapper {
    public VirtualTelegramBinding selectById(Long id);

    public VirtualTelegramBinding selectByTelegramUserId(Long telegramUserId);

    public VirtualTelegramBinding selectByUserId(Long userId);

    public VirtualTelegramBinding selectByBindCode(String bindCode);

    public List<VirtualTelegramBinding> selectVirtualTelegramBindingList(VirtualTelegramBinding binding);

    public int insertVirtualTelegramBinding(VirtualTelegramBinding binding);

    public int updateVirtualTelegramBinding(VirtualTelegramBinding binding);
}
