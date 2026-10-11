# AGENTS.md

工作区现在**只有一个项目**：`photo-answer-android/`（拍照答题 App）。
曾有的 `npk-android/`、`rom/`、`《重装上阵》解包工具。/`、`ai-homework/` 都已被删除 ——
旧文档里提到它们的部分一律作废，别照着去找。

## 构建

```bash
cd photo-answer-android && bash build.sh          # = assembleRelease，全量约 3 分钟
```
产物 `app/build/outputs/apk/release/app-release.apk`，已用 `photoanswer.keystore` 签 v2+v3。
发新版**必须升 `versionCode`**（当前 4 / versionName 1.3）。

- **aarch64 坑（最容易踩）**：AGP 自带的 aapt2 是 x86_64，本机跑不了。
  只有 `/root/android/sdk/build-tools/35.0.0/aapt2` 是 aarch64 版。
  该 override 已写进 `/root/.gradle/gradle.properties`，`build.sh` 也传了一次 —— 别删。
- Gradle 8.7 wrapper（`gradlew`）。缓存里有 8.14 / 9.4.1，**别用 9.x**，AGP 8.5.1 不兼容。
- recyclerview **必须 1.3.2**（1.1.0 没有 `getBindingAdapterPosition`，编译直接失败）。
- 没设备、没 ADB：**任何功能都无法真机验证**，只能静态核对（编译 + `apksigner verify` + `aapt2 dump badging` + 查 dex 类名）。

## 改代码前必知的三条契约

1. **答案格式**：提示词要求模型每题前单独写一行 `#第N题`，客户端靠 `Answers.split()` 切题显示。
   改 `Prompt.solve()` 时这个标记不能丢，正则要同步改 `Answers.MARK`。
2. **图片清晰度**：上传长边 **2048**、质量从 95 起自适应降级，别压回 1280/q82 ——
   作业本的字很小，低质量 JPEG 会把文字边缘糊掉、模型认不出（用户实测反馈过）。
   参数在 `ImageUtil.UPLOAD_MAX_SIDE` / `QUALITY_STEPS`。
3. **动画用 Material 官方 motion**（`MaterialSharedAxis` / `MaterialFadeThrough`），不要手写补间。

## Git

- 分支 `main`，现有 2 个 commit。**提交前先确认**：当前跟踪了 617 个文件，
  但其中 530 个是 `app/build/` 产物、26 个是 `.gradle/` 缓存，源码只有 52 个。
  `.gitignore` 现在只有 `/.aicode/` 和 `/.gitignore` 两行，**没有排除 build 产物**。
  新增提交前应先补 `app/build/`、`.gradle/`、`*.apk`，别再往里塞二进制。
- 未经主人确认**不要代跑 `git add` / `commit` / `push` / `reset`**。

## 其他

- `python3` 没有 pip，装不了第三方包。
- Bash 工具跑的是 dash，**不支持 `{}` 花括号展开**，写脚本用 POSIX 写法。
- 项目专属约定与踩坑记在 `~/.aicode/memory/photo-answer-android.md`，开工前值得读一眼。
