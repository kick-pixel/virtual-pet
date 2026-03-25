package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.VirtualUser;
import com.ruoyi.system.domain.VirtualUserAdminVO;

/**
 * 虚拟宠物平台用户Mapper接口
 */
public interface VirtualUserMapper {
    public VirtualUser selectVirtualUserById(Long userId);

    public VirtualUser selectVirtualUserByUsername(String username);

    public VirtualUser selectVirtualUserByEmail(String email);

    public List<VirtualUser> selectVirtualUserList(VirtualUser virtualUser);

    public int insertVirtualUser(VirtualUser virtualUser);

    public int updateVirtualUser(VirtualUser virtualUser);

    public int updateLoginSuccess(VirtualUser virtualUser);

    public int updateLoginAttempts(VirtualUser virtualUser);

    public int deleteVirtualUserById(Long userId);

    public VirtualUser selectVirtualUserByTelegramId(String telegramId);

    // ---- 管理后台新增方法 ----

    /** 管理后台用户列表（含中间表 JOIN） */
    List<VirtualUserAdminVO> selectAdminList(VirtualUserAdminVO query);

    /** 更新禁止生成状态 */
    int updateGenDisabled(@Param("userId") Long userId, @Param("genDisabled") Integer genDisabled);

    /** 更新账号状态 */
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /** 更新连续签到天数和历史最长天数 */
    int updateStreakDays(@Param("userId") Long userId,
                        @Param("consecutiveDays") Integer consecutiveDays,
                        @Param("maxStreakDays") Integer maxStreakDays);

    /** 更新 register_ip（首次注册时写入） */
    int updateRegisterIp(@Param("userId") Long userId, @Param("registerIp") String registerIp);

    /** 更新 country_region（首次写入，幂等） */
    int updateCountryRegion(@Param("userId") Long userId, @Param("countryRegion") String countryRegion);
}
