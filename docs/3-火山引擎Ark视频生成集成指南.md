# 火山引擎 Ark 视频生成集成指南

## 概述

本项目已集成火山引擎 Ark 平台的 **Seedance 视频生成模型**，支持以下功能：

- **文生视频**：根据文本提示词生成视频
- **图生视频**：基于首帧图片和提示词生成视频
- **首尾帧生视频**：根据首尾关键帧生成过渡视频
- **多参考图生视频**：基于多张参考图生成视频（仅 lite-i2v 支持）
- **音画同生**：生成带音频的视频（仅 1.5 pro 支持）

## 支持的模型

| 模型 ID | 能力 | 输出格式 | 推荐场景 |
|--------|------|---------|---------|
| `doubao-seedance-1-5-pro-251215` | 音画同生 + 图生视频 + 文生视频 | 480p/720p/1080p, 24fps, 4-12秒 | 最高品质，支持音频 |
| `doubao-seedance-1-0-pro-250528` | 图生视频 + 文生视频 | 480p/720p/1080p, 24fps, 2-12秒 | 标准版，平衡品质和速度 |
| `doubao-seedance-1-0-pro-fast-251015` | 图生视频 + 文生视频 | 同上 | 快速生成，注重速度 |
| `doubao-seedance-1-0-lite-t2v-250428` | 文生视频 | 同上 | 轻量文生视频 |
| `doubao-seedance-1-0-lite-i2v-250428` | 多参考图 + 首尾帧 + 图生视频 | 同上 | 轻量图生视频 |

## 配置步骤

### 1. 获取 API Key

1. 访问 [火山引擎控制台](https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey)
2. 创建 API Key
3. 复制密钥备用

### 2. 开通模型服务

访问 [开通管理页面](https://console.volcengine.com/ark/region:ark+cn-beijing/openManagement) 开通所需的 Seedance 模型。

### 3. 配置 application-pet.yml

在 `pet-platform/ruoyi-admin/src/main/resources/application-pet.yml` 中配置：

```yaml
pet:
  ai:
    ark:
      # 火山引擎 API 密钥
      api-key: your-ark-api-key-here
      # API 基础 URL（默认北京区域）
      base-url: https://ark.cn-beijing.volces.com/api/v3
      # 默认模型
      default-video-model: doubao-seedance-1-5-pro-251215
      # 请求超时（秒）
      timeout: 60
      # 轮询间隔（秒）
      poll-interval: 10
      # 最大轮询次数
      max-poll-attempts: 360
      # 是否启用水印（默认关闭）
      watermark-enabled: false
      # 是否启用音频（默认关闭，仅 1.5 pro 支持）
      audio-enabled: false
```

### 4. 环境变量配置（推荐）

为了安全，建议通过环境变量配置 API Key：

**Linux/Mac:**
```bash
export ARK_API_KEY="your-ark-api-key-here"
```

**Windows CMD:**
```cmd
setx ARK_API_KEY "your-ark-api-key-here"
```

**Windows PowerShell:**
```powershell
$env:ARK_API_KEY = "your-ark-api-key-here"
```

配置文件中使用：
```yaml
api-key: ${ARK_API_KEY:}
```

## 使用示例

### 1. 文生视频

**Java 代码示例：**

```java
VideoGenerationRequest request = new VideoGenerationRequest();
request.setProvider("ark");  // 指定使用火山引擎
request.setModel("doubao-seedance-1-5-pro-251215");
request.setPrompt("写实风格，晴朗的蓝天之下，一大片白色的雏菊花田，镜头逐渐拉近");
request.setDuration(5);  // 5秒
request.setRatio("16:9");  // 宽高比
request.setWatermark(false);  // 不添加水印

VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);
```

**REST API 调用：**

```bash
curl -X POST http://localhost:8181/ai/video/create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "ark",
    "model": "doubao-seedance-1-5-pro-251215",
    "prompt": "写实风格，晴朗的蓝天之下，一大片白色的雏菊花田",
    "duration": 5,
    "ratio": "16:9",
    "watermark": false
  }'
```

### 2. 图生视频（首帧）

```java
VideoGenerationRequest request = new VideoGenerationRequest();
request.setProvider("ark");
request.setModel("doubao-seedance-1-5-pro-251215");
request.setPrompt("女孩抱着狐狸，女孩睁开眼，温柔地看向镜头");
request.setImageUrl("https://example.com/first-frame.png");  // 首帧图片
request.setDuration(5);
request.setRatio("adaptive");  // 自适应比例
request.setGenerateAudio(true);  // 生成音频（仅 1.5 pro 支持）

VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);
```

### 3. 首尾帧生视频

```java
VideoGenerationRequest request = new VideoGenerationRequest();
request.setProvider("ark");
request.setModel("doubao-seedance-1-0-pro-250528");
request.setPrompt("360度环绕运镜");
request.setImageUrl("https://example.com/start-frame.png");  // 首帧
request.setEndFrameImageUrl("https://example.com/end-frame.png");  // 尾帧
request.setDuration(5);

VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);
```

### 4. 多参考图生视频

```java
VideoGenerationRequest request = new VideoGenerationRequest();
request.setProvider("ark");
request.setModel("doubao-seedance-1-0-lite-i2v-250428");  // 使用 lite-i2v
request.setPrompt("[图1]戴着眼镜的男生和[图2]的小狗，坐在[图3]的草坪上，视频卡通风格");
request.setReferenceImageUrls(Arrays.asList(
    "https://example.com/person.png",
    "https://example.com/dog.png",
    "https://example.com/grass.png"
));
request.setDuration(5);

VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);
```

### 5. 查询任务状态

```java
// 通过任务 ID 查询
AiVideoTask task = aiVideoService.queryTaskStatus(taskId);

// 通过任务 UUID 查询
AiVideoTask task = aiVideoService.queryTaskByUuid(taskUuid);

// 同步最新状态（从提供商获取）
AiVideoTask task = aiVideoService.syncTaskStatus(taskId);

System.out.println("状态: " + task.getStatus());
System.out.println("进度: " + task.getProgress() + "%");
if ("completed".equals(task.getStatus())) {
    System.out.println("视频URL: " + task.getOssVideoUrl());
}
```

## VideoGenerationRequest 参数说明

### 通用参数

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|-----|------|-----|------|--------|
| `provider` | String | 否 | 提供商名称（"ark" 或 "openai"） | 第一个可用提供商 |
| `model` | String | 否 | 模型 ID | 配置文件中的默认模型 |
| `prompt` | String | 是 | 文本提示词 | - |
| `duration` | Integer | 否 | 视频时长（秒），范围 2-12 | 10 |

### 火山引擎特有参数

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|-----|------|-----|------|--------|
| `ratio` | String | 否 | 视频比例（"16:9", "9:16", "adaptive"） | "16:9" |
| `imageUrl` | String | 否 | 首帧图片 URL | - |
| `endFrameImageUrl` | String | 否 | 尾帧图片 URL | - |
| `referenceImageUrls` | List<String> | 否 | 多参考图 URL 列表 | - |
| `generateAudio` | Boolean | 否 | 是否生成音频（仅 1.5 pro） | false |
| `watermark` | Boolean | 否 | 是否添加水印 | false |

## 视频比例说明

- `"16:9"`: 横屏视频（1920x1080）
- `"9:16"`: 竖屏视频（1080x1920）
- `"adaptive"`: 自适应比例（根据输入图片自动调整）

## 任务状态说明

| 状态 | 说明 |
|-----|------|
| `pending` | 任务已创建，等待处理 |
| `queued` | 任务已排队 |
| `processing` / `running` | 任务处理中 |
| `completed` / `succeeded` | 任务成功完成 |
| `failed` / `error` | 任务失败 |
| `cancelled` | 任务已取消 |

## 事件系统

系统会自动发布以下事件：

1. **VideoGenerationCreatedEvent**: 任务创建时
2. **VideoGenerationStatusChangedEvent**: 状态变化时
3. **VideoGenerationCompletedEvent**: 任务完成时（视频已上传到 OSS）
4. **VideoGenerationFailedEvent**: 任务失败时

可以通过监听这些事件来实现业务逻辑（如扣费、通知等）。

## 定时任务

系统会自动运行定时任务同步视频生成状态：

- **AiVideoTaskScheduler**: 每 30 秒扫描处理中的任务，同步状态并下载完成的视频

## 提示词编写建议

### 文生视频

1. **场景描述**：详细描述场景环境、天气、光线
2. **主体描述**：描述主体的外观、动作、表情
3. **镜头运镜**：指定镜头运动方式（推镜、拉镜、环绕等）
4. **风格说明**：指定视频风格（写实、动漫、水墨等）

示例：
```
写实风格，晴朗的蓝天之下，一大片白色的雏菊花田，镜头逐渐拉近，
最终定格在一朵雏菊花的特写上，花瓣上有几颗晶莹的露珠
```

### 图生视频

1. **动作描述**：描述图片中主体的动作变化
2. **镜头变化**：描述镜头如何移动
3. **环境变化**：描述环境的动态变化

示例：
```
女孩抱着狐狸，女孩睁开眼，温柔地看向镜头，狐狸友善地抱着，
镜头缓缓拉出，女孩的头发被风吹动，可以听到风声
```

### 音画同生（1.5 pro）

在提示词中可以描述期望的音效：

```
镜头围绕人物推镜头拉近，特写人物面部，她正在用京剧唱腔唱"月移花影，疑是玉人来"，
唱词充满情感，唱腔充满传统京剧特有的韵味与技巧
```

## 最佳实践

### 1. 选择合适的模型

- **追求最高品质 + 音频**：使用 `doubao-seedance-1-5-pro-251215`
- **平衡品质和成本**：使用 `doubao-seedance-1-0-pro-250528`
- **快速生成**：使用 `doubao-seedance-1-0-pro-fast-251015`
- **多参考图**：使用 `doubao-seedance-1-0-lite-i2v-250428`

### 2. 视频时长建议

- **首次测试**：使用 5 秒
- **正式使用**：根据场景需要，范围 2-12 秒
- **注意成本**：时长越长，成本越高

### 3. 图片要求

- **格式**：支持 PNG、JPG
- **大小**：建议 2MB 以内
- **分辨率**：建议 1920x1080 或更高
- **内容**：清晰、主体明确

### 4. 错误处理

```java
try {
    VideoGenerationResult result = aiVideoService.createVideoTask(request, userId);
    if ("failed".equals(result.getStatus())) {
        log.error("创建任务失败: {} - {}", result.getErrorCode(), result.getErrorMessage());
        // 处理错误
    }
} catch (Exception e) {
    log.error("调用服务失败", e);
    // 处理异常
}
```

### 5. 轮询策略

系统会自动同步任务状态，但如果需要手动查询：

```java
// 不建议：频繁轮询会增加 API 调用成本
while (!"completed".equals(task.getStatus())) {
    Thread.sleep(10000);  // 等待 10 秒
    task = aiVideoService.syncTaskStatus(taskId);
}

// 建议：使用事件监听器
@EventListener
@Async("eventExecutor")
public void handleVideoCompleted(VideoGenerationCompletedEvent event) {
    // 处理完成逻辑
}
```

## 成本优化建议

1. **使用合适的模型**：根据需求选择，不要盲目使用最高级模型
2. **控制视频时长**：不要生成不必要的长视频
3. **批量处理**：合并多个请求，减少 API 调用次数
4. **缓存结果**：相同提示词可以复用生成结果

## 常见问题

### Q: 为什么任务一直是 processing 状态？

A: 视频生成是一个异步过程，可能需要几分钟到十几分钟。系统会自动轮询状态，请耐心等待。

### Q: 可以取消正在生成的任务吗？

A: 火山引擎 API 暂不支持取消任务，但可以在系统中标记为已取消状态。

### Q: 如何同时使用 OpenAI 和火山引擎？

A: 在创建任务时通过 `provider` 参数指定：
```java
request.setProvider("ark");     // 使用火山引擎
request.setProvider("openai");  // 使用 OpenAI
```

### Q: 视频生成失败如何处理？

A: 系统会自动发布 `VideoGenerationFailedEvent` 事件，可以监听此事件进行重试或通知。

## 参考链接

- [火山引擎 Ark 控制台](https://console.volcengine.com/ark)
- [Seedance 模型文档](https://www.volcengine.com/docs/82379/1366799)
- [API Key 管理](https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey)
- [提示词指南](https://www.volcengine.com/docs/82379/1631633)
