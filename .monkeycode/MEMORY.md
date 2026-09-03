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

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: Discovered by Agent during 云构建 ipa 调通（用户仓库 93951070/Ioswy，10 轮迭代成功）
- Category: Build Methods | Troubleshooting & Debugging
- Instructions:
  - K/N interop 避坑：AVFoundation/AVFAudio 必须**通配 import**（platform.AVFoundation.*——部分方法生成为包内扩展函数，显式 import 类名会全量 unresolved）；NS_ENUM 用顶层常量（AVPlayerTimeControlStatusPlaying/AVPlayerItemStatusReadyToPlay）；replaceCurrentItem → replaceCurrentItemWithPlayerItem；AVPlayerItem(nsUrl) 位置传参构造；NSUserDefaults.setObject(v, forKey = k)；AVQueuePlayer 父类方法链断，直接用 AVPlayer（队列手动管理不需要它）；AVAudioSession 在 platform.AVFAudio 包
  - 本地(环境)变量能查证的 K/N 符号用 find ~/.konan -name "*.klib"——本项目环境无 K/N 发行版，只能靠云构建迭代
  - K/N linkReleaseFramework OOM（7GB macos-14 runner）：去 --no-daemon + kotlin.native.daemon.jvmargs=-Xmx6g 后 Release 仍 OOM，**最终用 linkDebugFrameworkIosArm64**（跳过 -O 优化内存骤降，侧载测试够用）；恢复 Release 需更大 runner
  - CI gradle 无 local.properties 时签名配置读取会炸：getLocalValue 里先判文件存在返回占位
  - commonMain 里禁止 JVM-only API：LocalContext（coil3 直接传 url string）、String.format（padStart 拼接）、toSortedMap（entries.sortedBy+associate）——Android 编译不暴露这些错误，只有 iOS 编译会炸
  - K/N 顶层函数生成的 ObjC 类名 = **文件名**Kt（IosRoot.kt → IosRootKt），不是模块名
  - xcodebuild CONFIGURATION_BUILD_DIR 相对路径按 **项目目录**（iosApp/）解析，取产物用 iosApp/build/Debug-iphoneos/
  - 用户仓库 push 后 workflow 可能不自动触发（首推新增 workflow 的 quirk），用 API dispatch：POST /repos/{owner}/{repo}/actions/workflows/ios-build.yml/dispatches {"ref":"main"}
  - 推送用一次性 URL git push https://TOKEN@github.com/...（不写入 .git/config）

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: iOS 多轮云构建失败修复过程中发现（qrose 二维码/设置页/分享接线）
- Category: Build Methods | Troubleshooting & Debugging
- Instructions:
  - **Linux 本地可跑 `./gradlew :shared:compileKotlinIosSimulatorArm64` 验证 iosMain 代码编译**（klib 编译可行；完整 framework link 仍需 macOS 云构建）——改 iosMain 后先本地跑这个，能消灭大部分 K/N unresolved 错误，避免 10 分钟一轮的云构建试错
  - Compose `by collectAsState()`/`by remember{mutableStateOf}` 委托属性禁止 smart cast：`if (x != null)` 后直接访问 x.field 会报 "Smart cast is impossible, because x is a delegated property"——必须先赋局部变量 `val v = x` 再判空使用；生成代码批量排查 `by collectAsState|by remember`
  - K/N 二维码生成：CoreImage→CGImage→UIImage→PNG→Skia 的 interop 链坑多（qrCodeGenerator/PNGData/toComposeImageBitmap 全 unresolved），直接用 qrose 库（io.github.alexzhirkevich:qrose:1.0.1，纯 KMP klib，`rememberQrCodePainter(content)` 返回 Painter 配合 Image）
  - iOS Assets/图标/启动屏：纯 plist 方案（UILaunchScreen dict 配 UIColorName+UIImageName 指向 Assets.xcassets）；手写 pbxproj 必须注册 PBXResourcesBuildPhase + PBXBuildFile + PBXFileReference（folder.assetcatalog），否则 actool 不打包、图标静默失效
   - 双端登录态注入：SharedNet.cookie/baseUrl 是共享层唯一真值镜像，Android 端 UserServiceImpl.login/logout + MusicApplication.onCreate 必须同步（AccountPreference 为真值来源），否则登录/红心/评论/推荐全链路 401

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: Discovered by Agent while performing 歌手域并行开发编译验证
- Category: Workflow & Collaboration | Troubleshooting & Debugging
- Instructions:
  - 多 agent 并行写 shared 模块时 :app:compileDebugKotlin 会撞上其他域的中间态编译错误（报错文件属其他域如 mine/album/mv 时，等 3-5 分钟对方稳定后重试，勿修改他人文件）
  - Gradle Kotlin 增量编译按内容 hash 判定 UP-TO-DATE，touch 同内容文件不触发重编；确认编译产物直接查 shared/build/tmp/kotlin-classes/debug 与 app/build/tmp/kotlin-classes/debug 下的类文件
  - decodeBean 的 fixLegacyFields 递归替换所有层级的 "artists"→"ar"：新接口返回体含顶层 artists 数组时（如 /artist/list），bean 必须用 @SerialName("ar") 接收

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: 电台/播客域开发前用 curl 实测接口返回结构时发现
- Category: Environment Configuration
- Instructions:
  - 本地网易接口代理运行在 http://127.0.0.1:3000（NedeaseCloudMusicApi 兼容），可 curl 实测真实返回结构后再建 bean
  - 推荐节目接口实际路径是 program/recommend（带斜杠），任务描述里的 program_recommend 下划线写法会 404
  - dj/recommend 返回 {djRadios, name, code} 无 hasMore/count；dj/hot、dj/sublist 返回 {djRadios, hasMore, code}；dj/program 返回 {count, code, programs}；dj/detail 返回 {code, msg, data:{...subed 直接在 data 上}}
  - SharedJson.decodeBean 的 fixLegacyFields 会把整棵 JSON 树里所有 duration 键改名为 dt，电台节目(DjProgramData)自身时长字段需用 @SerialName("dt") 接收
  - 工作区存在多 Agent 并行开发：编译失败先看错误归属域，可能是他域在途中间态，等待重试即可；gradle 编译用 --rerun 才能绕过内容哈希 UP-TO-DATE 强制验证

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: MV 内嵌播放器 iOS 编译失败排查时发现（本地编译过但云端失败）
- Category: Build Methods | Environment Configuration
- Instructions:
  - 本地是 Linux，KGP 会自动禁用全部 K/N 编译任务（compileKotlinIosSimulatorArm64 显示 SKIPPED, enabled=false）：本地跑 iOS 编译永远假成功，iosMain 代码只有云端 macOS runner 能真验证
  - iOS 编译错误必须靠云端构建日志定位：下载 run logs zip 后 grep "e: file:"；修完直接推云端验证，本地无 iOS 编译能力
   - K/N 编写规范（踩坑汇总）：ObjC 框架 import 用通配（platform.AVFoundation.*）；NSURL.URLWithString 可空需判 null 后再传；AVPlayer 调用写顺序语句（apply 嵌套 lambda 会触发类型推断失败）；UIKitView 只用 modifier/factory/update 三参（onRelease 签名跨版本易错），释放用 DisposableEffect

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: 评论域升级 comment/new + 楼中楼时用 curl 实测本地接口发现
- Category: Environment Configuration | Troubleshooting & Debugging
- Instructions:
  - comment/new 的 cursor 是不透明字符串（时间戳或 normalHot#N），原样回传即可；sortType=3/2 按 cursor 正常翻页，sortType=1（推荐）翻页怪异：page1 只回少量条目，page2 一次性吐完剩余并 hasMore=false——列表层靠 hasMore 守卫停住即可，勿按 pageSize 假设条数
  - comment/floor 的翻页游标 time = 本页最后一条回复的时间戳（Long），原样回传；parentCommentId 必须真实存在否则 code 400
  - 测试 cookie 对歌曲发表/回复评论（comment t=1/t=2）均被 401「无法评论该资源」拒绝，是账号权限限制；发送链路只能验证参数形态与错误信息冒泡
  - CommentItem bean 早期没有 replyCount 字段（接口实际有），新增评论展示字段前先 curl 核对再补 bean

[Project Knowledge Summary]
- Date: 2026-09-03
- Context: 排查「最近播放」页面周排行/累计区块空白时 curl 实测发现
- Category: Troubleshooting & Debugging | Environment Configuration
- Instructions:
  - user/record 接口字段名与内容错位：type=0 时周数据放在 allData 键，type=1 时累计数据放在 weekData 键——解析时按 type 反读，勿按字段名直觉取
  - 本地 NeteaseCloudMusicApi 无 play/record 路由（404），最近播放歌曲记录只有 user/record
  - playListListData bean 的顶层键差异：/user/playlist（我的歌单）返回 playlist 单数，/top/playlist（歌单广场）返回 playlists 复数，/toplist 返回 list——bean 需同时兼容三键（用 all getter 兜底）

[Project Knowledge Summary]
- Date: 2026-09-03
- Context: 排查「喜欢的音乐」歌单详情页开很久才出数据时 curl 实测发现
- Category: Troubleshooting & Debugging | Environment Configuration
- Instructions:
  - /playlist/detail 对「我喜欢的音乐」歌单也会返回完整 tracks（SongData 列表），且 trackCount 不等于 tracks.size（差了重复排序），tracks 才是全量
  - 喜欢音乐/大歌单详情加载慢的根因：PlaylistViewModel.loadData 先拿 detail 再跑 getFullPlaylistSongList（分页循环 /playlist/track/all limit=800）串行后才设 state，整个页面等慢接口。isLike 时可直接用 detail.playlist.tracks 跳过第二次分页拉取
  - 分页拉全量接口用 SongListData.songs 判空停止；limit=800 一页，offset=累计 size
  - 应用重启首请求易遇一次性网络未就绪失败：MineViewModel.updatePlaylist 用 repeat(2)+delay(1200) 兜底重试一次，避免「重启须点一下才加载」

[Project Knowledge Summary]
- Date: 2026-09-03
- Context: 实现桌面歌词时厘清跨 App 悬浮的系统能力边界
- Category: Troubleshooting & Debugging | Environment Configuration
- Instructions:
  - iOS 系统级禁止在任意其他 App 之上悬浮窗口（无 API、审核不过），网易云/QQ音乐的 iOS「桌面歌词」= App 内歌词页 + 锁屏/控制中心歌词行（MPNowPlayingInfoCenter 的 subtitle 小字），并非真悬浮。跨 App 悬浮歌词是 Android 专属（android.permission.SYSTEM_ALERT_WINDOW + WindowManager TYPE_APPLICATION_OVERLAY）
  - Android「桌面歌词悬浮窗」架构：MusicService(:MediaSessionService，播放期间前台常驻) 持有 LyricFloatWindow（app 模块原生 View，非 Compose），用 WindowManager.addView 挂 TYPE_APPLICATION_OVERLAY，观察 PlayerController.currentSong/playProgress + DiscoverNet.getLrc 拉歌词；可拖动/点击进播放页/关闭；desktopLyricsOn 开关经 SnapshotStateObserver.observeReads(scope, onValueChangedForScope, block) 触发 syncLyricWindow 显示/隐藏
  - MusicService 获取 PlayerController：import me.wcy.music.service.PlayServiceModule.playerController 扩展（Application.playerController()），不在 Service 里直接 new
  - Compose runtime 1.7.x SnapshotStateObserver 无 addObservedState/dispose；用 observeReads(scope, onValueChangedForScope, block)，清理用 stop()+clear()；onValueChangedForScope 回调签名带 scope 参数可忽略
  - Android 端开启桌面歌词需引导用户到 Settings.ACTION_MANAGE_OVERLAY_PERMISSION（Settings.canDrawOverlays 判断），MainActivity.toggleDesktopLyrics 已处理
  - iOS 锁屏歌词后台有效的正确做法：歌词行由引擎驱动，UI setLrcLines(LrcLine) 推到 IosPlayerEngine，0.5s AVPlayer 周期回调 updateNowPlaying() 里按进度取当前行写 subtitle——Compose 只在 App 前台推一次歌词，后台停止重组仍能随进度刷新。替代旧的 Compose LaunchedEffect 逐帧推 subtitle（后台会冻结）

