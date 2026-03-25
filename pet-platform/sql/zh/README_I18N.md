# 国际化实施说明文档

## 📋 项目概述

本文档说明虚拟宠物AI平台的国际化实施方案，目前支持中文和英文，并预留了韩文扩展接口。

---

## ✅ 已完成的工作

### 1. 前端国际化（Vue3 + vue-i18n）

#### 安装和配置
- ✅ 安装 vue-i18n@9 依赖
- ✅ 创建语言文件目录结构：[pet-web/src/lang/](../../../pet-web/src/lang/)
- ✅ 配置 [main.js](../../../pet-web/src/main.js) 集成 vue-i18n
- ✅ 动态切换 Element Plus 语言包

#### 翻译包
- ✅ [zh-CN.js](../../../pet-web/src/lang/zh-CN.js) - 中文翻译（~500行）
- ✅ [en-US.js](../../../pet-web/src/lang/en-US.js) - 英文翻译（~500行）
- ✅ [ko-KR.js](../../../pet-web/src/lang/ko-KR.js) - 韩文翻译（预留）

#### 语言切换功能
- ✅ [LangSelect](../../../pet-web/src/components/LangSelect/index.vue) 组件
- ✅ 集成到 [Navbar.vue](../../../pet-web/src/layout/components/Navbar.vue) 导航栏
- ✅ 语言设置持久化到 localStorage
- ✅ 所有 HTTP 请求自动添加 `?lang=` 参数

#### 菜单国际化
- ✅ [SidebarItem.vue](../../../pet-web/src/layout/components/Sidebar/SidebarItem.vue) 支持 i18n key
- ✅ 向后兼容（支持直接文本和 i18n key 两种方式）

#### 菜单管理页面
- ✅ [menu/index.vue](../../../pet-web/src/views/system/menu/index.vue) 支持编辑 i18n_key 字段
- ✅ 表格中显示国际化KEY列
- ✅ 表单中添加国际化KEY输入框

### 2. 后端国际化（Spring Boot）

#### 数据库扩展
- ✅ 创建升级脚本：[i18n_upgrade.sql](i18n_upgrade.sql)
  - 为 `sys_menu` 表添加 `i18n_key` 字段
  - 更新60+ 个菜单项的 i18n_key
- ✅ 修改 [SysMenu.java](../../ruoyi-common/src/main/java/com/ruoyi/common/core/domain/entity/SysMenu.java) 实体类
- ✅ 修改 [SysMenuMapper.xml](../../ruoyi-system/src/main/resources/mapper/system/SysMenuMapper.xml) 查询语句
- ✅ 修改 [SysMenuServiceImpl.java](../../ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysMenuServiceImpl.java) 路由构建逻辑

#### 国际化资源文件
- ✅ [messages_zh_CN.properties](../../ruoyi-admin/src/main/resources/i18n/messages_zh_CN.properties) - 中文错误消息
- ✅ [messages_en_US.properties](../../ruoyi-admin/src/main/resources/i18n/messages_en_US.properties) - 英文错误消息
- ✅ [messages_ko_KR.properties](../../ruoyi-admin/src/main/resources/i18n/messages_ko_KR.properties) - 韩文消息（预留）

#### 代码优化
- ✅ 修改 [Xss.java](../../ruoyi-common/src/main/java/com/ruoyi/common/xss/Xss.java) 注解，使用国际化KEY替代中文常量

---

## 🚀 使用说明

### 部署步骤

#### 1. 数据库升级
对于已有数据库，执行升级脚本：
```bash
mysql -u root -p your_database < i18n_upgrade.sql
```

#### 2. 启动后端服务
```bash
cd pet-platform/ruoyi-admin
mvn spring-boot:run
```

#### 3. 启动前端服务
```bash
cd pet-web
npm install
npm run dev
```

### 语言切换

#### 前端切换语言
1. 点击导航栏右上角的语言切换按钮
2. 选择 "简体中文" 或 "English"
3. 页面会自动刷新并切换语言

#### 后端API语言切换
所有前端请求会自动带上 `?lang=zh_CN` 或 `?lang=en_US` 参数。

后端会根据此参数返回对应语言的错误消息。

### 菜单国际化配置

#### 在后台管理系统中配置
1. 登录后台管理系统
2. 进入 **系统管理 > 菜单管理**
3. 编辑菜单项，填写 **国际化KEY** 字段
   - 格式：`menu.system.user`（使用点号分隔）
   - 如不填写，则使用菜单名称

#### 前端翻译文件配置
在 [zh-CN.js](../../../pet-web/src/lang/zh-CN.js) 和 [en-US.js](../../../pet-web/src/lang/en-US.js) 中添加对应的翻译：

```javascript
// zh-CN.js
export default {
  menu: {
    system: {
      user: '用户管理',
      role: '角色管理',
      // ...
    }
  }
}

// en-US.js
export default {
  menu: {
    system: {
      user: 'User Management',
      role: 'Role Management',
      // ...
    }
  }
}
```

---

## 🎯 待完成工作

### 英文版SQL脚本创建

创建 `1.ry_20250522_en.sql` 英文版初始化脚本，需要翻译以下数据（共123+条）：

#### 需要翻译的表和字段

1. **sys_menu**（60+ 条记录）
   - `menu_name` - 菜单名称
   - 同时需要填写 `i18n_key` 字段

2. **sys_dict_type**（10 条记录）
   - `dict_name` - 字典名称
   - `remark` - 备注

3. **sys_dict_data**（27 条记录）
   - `dict_label` - 字典标签

4. **sys_post**（4 条记录）
   - `post_name` - 岗位名称
   - `remark` - 备注

5. **sys_dept**（10 条记录）
   - `dept_name` - 部门名称

6. **sys_role**（2 条记录）
   - `role_name` - 角色名称
   - `remark` - 备注

7. **sys_config**（8 条记录）
   - `config_name` - 参数名称
   - `remark` - 备注

8. **sys_notice**（2 条记录）
   - `notice_title` - 公告标题
   - `notice_content` - 公告内容

#### 翻译示例

**中文版**：
```sql
insert into sys_menu values(1, '系统管理', '0', 1, 'system', null, '', '', 1, 0, 'M', '0', '0', '', 'system', 'menu.system.title', 'admin', sysdate(), '', null, '系统管理目录');
```

**英文版**：
```sql
insert into sys_menu values(1, 'System', '0', 1, 'system', null, '', '', 1, 0, 'M', '0', '0', '', 'system', 'menu.system.title', 'admin', sysdate(), '', null, 'System Management');
```

---

## 📝 扩展新语言

### 添加日语（示例）

#### 1. 前端
```javascript
// pet-web/src/lang/ja-JP.js
export default {
  menu: {
    system: {
      title: 'システム管理',
      user: 'ユーザー管理',
      // ...
    }
  }
}
```

```javascript
// pet-web/src/lang/index.js
import jaJP from './ja-JP'

const i18n = createI18n({
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
    'ko-KR': koKR,
    'ja-JP': jaJP  // 新增
  }
})
```

#### 2. 后端
创建 `messages_ja_JP.properties`：
```properties
not.null=* 必須項目
user.login.success=ログイン成功
# ...
```

#### 3. 数据库（可选）
创建 `ry_20250522_ja.sql` 日语版脚本。

#### 4. 语言切换组件
在 [LangSelect/index.vue](../../../pet-web/src/components/LangSelect/index.vue) 中添加日语选项。

**预计时间**：2-3小时（假设翻译内容已准备好）

---

## 🔍 常见问题

### 1. 菜单显示为 key 而不是文本？
**原因**：前端翻译文件中缺少对应的 i18n key 翻译。

**解决**：
- 检查菜单的 `i18n_key` 字段
- 在 [zh-CN.js](../../../pet-web/src/lang/zh-CN.js) 和 [en-US.js](../../../pet-web/src/lang/en-US.js) 中添加对应翻译

### 2. 切换语言后 Element Plus 组件没有切换？
**原因**：Element Plus 语言包需要刷新页面才能生效。

**解决**：[LangSelect/index.vue](../../../pet-web/src/components/LangSelect/index.vue) 中已实现自动刷新功能。

### 3. 后端错误消息没有翻译？
**原因**：请求未带 `lang` 参数，或资源文件中缺少对应翻译。

**解决**：
- 检查 [request.js](../../../pet-web/src/utils/request.js) 是否正确添加 lang 参数
- 在 `messages_zh_CN.properties` 和 `messages_en_US.properties` 中添加翻译

---

## 📞 技术支持

如有问题，请参考：
- [RuoYi 官方文档](http://doc.ruoyi.vip/)
- [vue-i18n 文档](https://vue-i18n.intlify.dev/)
- [Element Plus 国际化](https://element-plus.org/zh-CN/guide/i18n.html)
