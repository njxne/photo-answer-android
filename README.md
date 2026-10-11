# 拍照答题

一个作业帮式的拍照答题 App。拍照后**直接给出标准答案**，不做讲解；同时支持改作文与 AI 生作文。

---

## 功能

### 拍题
- 支持**单张 / 多张**图片（最多 9 张），按顺序合并成一份答案
- 答案按题号拆分，顶部有**圆形按钮 1/2/3** 可切换查看
- 切换是**纯本地**的，不会重新请求 AI，不浪费额度
- 数学题用**递等式（脱式）计算**，每一步单独一行，最后写「答：……」
- 不出现「知识点 / 解析 / 思路」这类讲解内容

### 改作文
- 可以拍照，也可以直接粘贴文字
- 输出**得分、等级、优点、问题、逐段修改建议、修改后全文**

### AI 生作文
- 输入题目和要求，直接生成符合年级水平的完整作文

### 其他
- **年级设置**：1–12 年级，答案写法按小学 / 初中 / 高中三档自动调整
- **历史记录**：本地保存，可回看、复制、删除
- **自建相机**：用 CameraX 写的，不调用系统相机，支持闪光灯
- **分页动画**：用 Material 官方的 MaterialSharedAxis / MaterialFadeThrough
- **图片清晰**：上传长边 2048、质量从 95 起自适应，保证小字不糊
- **模型自动拉取**：设置页可一键获取可用模型列表并选择

---

## 内置提示词

提示词覆盖**全部题型**：

- **数学**：口算、竖式、递等式、简便运算、混合运算、估算、填空、判断、选择、单位换算、分数小数百分数、比和比例、找规律、应用题（行程 / 工程 / 盈亏 / 植树 / 鸡兔同笼 / 和差倍 / 年龄 / 折扣利率）、几何（周长 / 面积 / 体积 / 角度）
- **语文**：看拼音写词语、组词、近反义词、造句、病句修改、句式转换、关联词、标点、修辞、古诗默写、阅读理解
- **英语**：单词拼写、互译、选择、填空、句型转换、连词成句、词形变化、阅读理解
- **理化生**：实验步骤、单位换算、公式代入、判断选择
- **政史地**：填空、判断、选择、材料分析

---

## 使用方法

1. 打开「**我的**」页，填写 **接口地址**、**API Key** 和**模型名**（模型必须支持看图）
2. 回到「**拍题**」页，拍照或从相册选图
3. 点「**开始作答**」，答案出来后点顶部圆形按钮切换查看各题

---

## 构建

```bash
cd photo-answer-android && bash build.sh
```

产物路径：`app/build/outputs/apk/release/app-release.apk`

### 环境要求
- Gradle 8.7 + AGP 8.5.1
- JDK 17
- Android SDK 34
- minSdk 24（Android 7.0 及以上）

> ⚠️ 在 aarch64 机器上必须指定 aapt2 为 `/root/android/sdk/build-tools/35.0.0/aapt2`。
> AGP 自带的 aapt2 是 x86_64 版本，在 aarch64 上会直接崩溃（SIGILL）。

---

## 技术栈

| 项目 | 选型 |
|---|---|
| 构建 | Gradle 8.7 + AGP 8.5.1 |
| 界面 | Material Components 3（Material3，全部为官方控件，无自绘） |
| 相机 | CameraX 1.3.4 |
| 网络 | HttpURLConnection + OpenAI 兼容接口 |
| 图片 | 长边 2048，JPEG 质量自适应 |
| 存储 | SharedPreferences |

---

## 注意事项

- **API Key 只存在本机**，不会上传到别处
- 拍题依赖**视觉模型**（必须支持图片输入），请在设置页选对模型
- `photoanswer.keystore` 是签名文件，**请勿公开**；丢了将无法覆盖安装，用户只能卸载重装
- 每次作答都是**全新对话**，不带历史上下文，不会受上一份作业影响

---

---

# Photo Answer

A homework-help app in the style of Zuoyebang. Take a photo and get **the standard answer directly**, with no long explanations. It also supports essay grading and AI essay writing.

---

## Features

### Solve by Photo
- Supports **single or multiple** images (up to 9), merged into one answer in order
- Answers are split by question number, switchable via the **circular buttons 1/2/3** at the top
- Switching is **local only** — it does not re-query the AI, so no extra cost
- Math answers use **step-by-step (递等式) calculation**: one line per step, ending with a "答：" line
- No filler such as "knowledge points / analysis / hints"

### Grade Essay
- Take a photo or paste text directly
- Outputs **score, grade, strengths, issues, per-paragraph suggestions, and the revised full text**

### Write Essay
- Enter a topic and requirements to generate a complete essay matching the grade level

### Also
- **Grade setting**: grades 1–12, answer style adapts across primary / junior / senior levels
- **History**: stored locally, with view, copy and delete
- **Built-in camera**: built with CameraX, no system camera app, flash supported
- **Page transitions**: official Material motion (MaterialSharedAxis / MaterialFadeThrough)
- **Image sharpness**: uploaded at 2048px long edge with adaptive quality starting at 95, keeping small text readable
- **Model auto-fetch**: one tap in Settings to fetch and pick from the available model list

---

## Built-in Prompts

The prompts cover **all question types**:

- **Math**: mental arithmetic, column calculation, step-by-step, simplification, mixed operations, estimation, fill-in, true/false, multiple choice, unit conversion, fractions/decimals/percentages, ratios and proportions, pattern finding, word problems (travel / work / profit-loss / tree planting / chickens-and-rabbits / sum-difference-multiple / age / discount-interest), geometry (perimeter / area / volume / angles)
- **Chinese**: pinyin-to-words, word formation, synonyms and antonyms, sentence making, grammar correction, sentence transformation, conjunctions, punctuation, rhetoric, classical poetry dictation, reading comprehension
- **English**: spelling, translation, multiple choice, fill-in, sentence pattern conversion, word ordering, word form changes, reading comprehension
- **Science (Physics / Chemistry / Biology)**: lab procedures, unit conversion, formula substitution, true/false and multiple choice
- **Humanities (Politics / History / Geography)**: fill-in, true/false, multiple choice, material analysis

---

## Usage

1. Open the **Me** tab and fill in the **API base URL**, **API Key**, and **model name** (the model must support vision)
2. Go back to the **Solve** tab, take a photo or pick images from the gallery
3. Tap **Start**; once the answer appears, use the circular buttons at the top to switch between questions

---

## Build

```bash
cd photo-answer-android && bash build.sh
```

Output path: `app/build/outputs/apk/release/app-release.apk`

### Requirements
- Gradle 8.7 + AGP 8.5.1
- JDK 17
- Android SDK 34
- minSdk 24 (Android 7.0 and above)

> ⚠️ On aarch64 machines you must set aapt2 to `/root/android/sdk/build-tools/35.0.0/aapt2`.
> The aapt2 bundled with AGP is x86_64 and will crash immediately (SIGILL) on aarch64.

---

## Tech Stack

| Item | Choice |
|---|---|
| Build | Gradle 8.7 + AGP 8.5.1 |
| UI | Material Components 3 (Material3, all official widgets, no custom drawing) |
| Camera | CameraX 1.3.4 |
| Network | HttpURLConnection + OpenAI-compatible API |
| Image | 2048px long edge, adaptive JPEG quality |
| Storage | SharedPreferences |

---

## Notes

- The **API key is stored locally only** and never uploaded elsewhere
- Photo solving requires a **vision-capable model** (image input support); choose the right model in Settings
- `photoanswer.keystore` is the signing key — **keep it private**. If lost, you cannot push update installs and users must uninstall first
- Each request starts a **fresh conversation** with no prior context, so previous homework never affects the current answer
