# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Format

### User Instruction Entry
User instruction entries should follow this format:

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry
Entries discovered by the Agent during task execution should follow this format:

[Project Knowledge Summary]
- Date: [YYYY-MM-DD]
- Context: Discovered by Agent while performing [specific task description]
- Category: [Operations & Deployment|Build Methods|Testing Methods|Troubleshooting & Debugging|Workflow & Collaboration|Environment Configuration]
- Instructions:
  - [Specific knowledge points, described line by line]

## Deduplication Strategy
- Before adding a new entry, check for similar or identical instructions.
- If a duplicate is found, skip the new entry or merge it with the existing one.
- When merging, update the context or date information.
- This helps avoid redundant entries and keeps the memory file tidy.

## Entries

[User Instruction Summary]
- Date: 2026-09-01
- Context: 用户在 KMP/iOS 迁移进行中交付备份目录后指示
- Instructions:
  - 备份目录（/workspace/ponymusic-compose-backup/ 等）创建后不要再改动
  - 迁移类长任务授权自主推进："不用问我，一步到位"，每步保持编译全绿

[Project Knowledge Summary]
- Date: 2026-09-01
- Context: Discovered by Agent while attempting iOS framework 编译（linkDebugFrameworkIosSimulatorArm64）
- Category: Build Methods
- Instructions:
  - Kotlin 2.1.0 的 KGP 在 Linux 上对 iOS 目标：任务图存在但执行被强制 SKIPPED，kotlin.native.ignoreDisabledTargets=true 只隐藏警告、无法解除限制；本地只能编译 androidTarget 验证 commonMain
  - iOS framework/ipa 编译必须走 GitHub Actions macos-14 runner；workflow 位于 .github/workflows/ios-build.yml（xcodebuild -target iosApp + CODE_SIGNING_ALLOWED=NO 出未签名 ipa）
  - Xcode 工程在 /workspace/iosApp/（手写 pbxproj，Run Script 调 gradlew link*Framework* 后拷 Shared.framework 到 BUILT_PRODUCTS_DIR；OTHER_LDFLAGS 需 -ld64）
  - shared 模块迁移模式：app 的纯 Compose 文件按同包名直接 mv 进 shared/src/commonMain（跨模块 internal 要改 public）；图片加载用 coil3（io.coil-kt.coil3，crossfade 导入在 coil3.request.crossfade）

[Project Knowledge Summary]
- Date: 2026-09-01
- Context: Discovered by Agent during KMP 全量迁移收官（登录/设置/本地音乐/播放页/IosRoot）
- Category: Build Methods | Troubleshooting & Debugging
- Instructions:
  - Hilt @Binds 不能绑定 Kotlin object（无 @Inject 构造），object 单例要用独立 object module + @Provides 返回
  - KSP 有增量缓存陈旧问题：shared 新增类后 :app:kspDebugKotlin 报幽灵 "could not be resolved"，重跑或清缓存即过
  - shared iOS 侧必须依赖 ktor-client-darwin（HttpClient 无引擎会运行时崩），toml 已有 ktor-client-darwin 条目
  - Activity/Fragment `by viewModels<T> { factory }` 必须显式类型参数 T，否则推断失败
  - shared 侧抽象层现状：PlayerEngine（app=PlayerEngineBridge/Media3，ios=IosPlayerEngine/AVQueuePlayer）、UserSession（app=UserSessionBridge，ios=IosUserSession/NSUserDefaults）、SearchHistoryStore/MyCommentStore（接口+各端实现），绑定在 app/service/SharedBindingModule.kt
  - 迁移完成态：31 bean+网络层+13 个页面+组件全在 shared/commonMain；app 仅剩壳（Activity/Fragment 壳、MainScreen、Drawer、MainViewModel、二维码位图生成 QRUtils、MediaStore 扫描 LocalMusicLoader、歌词缓存 LrcCache）
  - iOS 云构建待验证风险：ObjC interop 枚举短名（AVPlayerItemStatus.ReadyToPlay 等）、AVPlayerItem.itemWithURL/AVAudioSession.setCategory 参数形式，若报错是单点小改
