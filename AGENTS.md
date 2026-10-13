# AGENTS.md

工作区只有一个项目：`photo-answer-android/`（拍照答题 App）。
曾有的 `npk-android/`、`rom/`、`《重装上阵》解包工具。/`、`ai-homework/` 均已删除，旧文档里提到它们的部分作废。

## 构建

```bash
cd photo-answer-android && bash build.sh      # = assembleRelease，全量 2–4 分钟
```

产物：`app/build/outputs/apk/release/app-release.apk`，脚本另复制到 `out/PhotoAnswer-v<版本>.apk`。
签名用 `photoanswer.keystore`（别名/口令均为 `photoanswer`）。发新版**必须升 versionCode**（当前 6 / 1.5）。

- **aarch64 坑**：AGP 自带 aapt2 是 x86_64，本机跑不了。唯一可用的是
  `/root/android/sdk/build-tools/35.0.0/aapt2`，已写进 `/root/.gradle/gradle.properties`
  且 `build.sh` 也传了一次 —— 别删。
- Gradle **8.7**（wrapper 已固定）。AGP 8.5.1 与 9.x 不兼容。
- recyclerview **必须 1.3.2**（1.1.0 缺 `getBindingAdapterPosition`，编译失败）。
- 没设备、没 ADB：功能无法真机验证，只能静态核对（编译 + `apksigner verify`
  + `aapt2 dump badging` + 查 dex 类名）。

### 液态玻璃依赖（首次构建前必须先做）

UI 用 [LiquidGlass](https://github.com/Abdullajon1881/LiquidGlass) 的 `liquidglass-view`（支持 View 系统）。
它**未发布到 Maven Central**，否则构建会报 `liquidglass-core FAILED`：

```bash
git clone --depth 1 https://github.com/Abdullajon1881/LiquidGlass.git && cd LiquidGlass
./gradlew :liquidglass-core:publishToMavenLocal :liquidglass-view:publishToMavenLocal
```

已在 `settings.gradle` 配 `mavenLocal()`。玻璃规则：玻璃节点须与 provider 是**兄弟**，不能放进 provider 内部。

## 改代码前必知的契约

1. **答案格式**：提示词要求模型每题前单独一行 `#第N题`，客户端靠 `Answers.split()` 切题。
   改 `Prompt.solve()` 不能丢这个标记，正则要同步改 `Answers.MARK`。
2. **图片清晰度**：上传长边 **2048**、质量从 95 起自适应降级，**别压回 1280/q82**
   （作业本字小，低质量 JPEG 会糊掉文字边缘，用户实测反馈过）。见 `ImageUtil`。
3. **UI 全部用现成库**，不自己画：Material Components 3 官方控件 + CameraX。
   题号按钮用官方 `Chip`（曾用自绘 `CircleNumberView`，已按用户要求移除）。
   动画用 Material 官方 motion（`MaterialSharedAxis` / `MaterialFadeThrough`），不手写补间。
4. **纯 Java 原生**，无 WebView / HTML / JS。玻璃库是 Kotlin，但通过 Gradle 混编调用没问题。

## Git

- 分支 `main`。**未经主人确认不要代跑 `git add` / `commit` / `push` / `reset`。**
- **`.gitignore` 的前导斜杠坑（已踩过）**：`/app/build/` 只匹配仓库根，挡不住
  `photo-answer-android/app/build/`（当前构建产物混进 646 个）。写规则**不要加前导斜杠**。
- 源码约 60 个。若发现跟踪了构建产物，用
  `git rm -r --cached <路径>` 移除（不删实际文件），再确认 `.gitignore` 生效后再提交。
- 分发用 APK 在 `photo-answer-android/out/`，`.gitignore` 用 `!` 单独解禁（`*.apk` 会先挡住）。

## 其他

- `python3` 无 pip，装不了第三方包。
- Bash 工具跑 dash，**不支持 `{}` 花括号展开**，脚本用 POSIX 写法。
- GitHub 凭据在 `~/.aicode/secret/github_token`（权限 600）；仓库 `njxne/photo-answer-android`。
- 项目踩坑记在 `~/.aicode/memory/photo-answer-android.md` 与 `vrchat-android9.md`，开工前值得读。
