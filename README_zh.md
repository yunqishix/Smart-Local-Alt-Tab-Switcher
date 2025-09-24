# 智能本地 Alt-Tab 切换器 (Smart Local Alt-Tab Switcher)

<p align="center">
  <a href="README.md">English</a> | <a href="README_zh.md">简体中文</a>
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**一个专为远程桌面用户设计的 Windows 小工具，旨在解决在全屏远程会话中 `Alt+Tab` 无法切换回本地桌面的痛点，提供无缝、无感的本地窗口切换体验。**

---

## 🧐 问题所在

当您在 Windows 系统中使用如 TeamViewer、向日葵、Raysync 等远程桌面软件，并将其全屏显示时，您会发现 `Alt+Tab` 快捷键会被远程系统捕获。这意味着您无法用这个最习惯的方式快速切换回本地电脑的应用程序（例如微信、浏览器或代码编辑器），只能通过 `Ctrl+Alt+Delete` 等繁琐操作退出全屏，严重影响工作效率和流畅度。

## ✨ 解决方案

**智能本地 Alt-Tab 切换器** 在后台静默运行，智能地监听键盘事件。当它检测到您在特定的远程桌面软件窗口中按下 `Alt+Tab` 时，它会巧妙地绕过远程系统的捕获，并执行一个核心操作：**将远程桌面的主窗口最小化**。

这个简单的动作带来的效果是革命性的：
*   **即时切换**：远程窗口最小化后，您本地的上一个活动窗口会立即显示出来。
*   **焦点自动转移**：键盘焦点会自然地转移到本地窗口，您可以立即开始操作。
*   **体验无缝**：整个过程就像使用原生的 `Alt+Tab` 一样流畅自然，几乎感觉不到工具的存在。

## 🚀 功能亮点

*   **🎯 智能识别**：自动识别多种主流远程桌面软件（如 Raysync, TeamViewer, 向日葵）以及以标准计算机名（如 `LAPTOP-XXXX`, `DESKTOP-XXXX`）为标题的窗口。
*   **🧠 智能决策**：能够处理远程软件的“内外双层”窗口结构。无论您是在远程画面的内层窗口还是在包含它的外壳窗口中操作，都能准确定位并最小化正确的目标。
*   **⚡️ 高度灵敏与容错**：采用基于时间戳的事件处理机制，即使您的 `Alt` 键按下和释放速度极快，也能准确捕捉到 `Alt+Tab` 意图，杜绝失灵。
*   **🤫 完全静默与无感**：程序在后台以极低的资源占用运行，没有任何可见的窗口或图标。一次设置，开机自启，让您彻底忘记它的存在。
*   **绿色便携**：打包为免安装的应用程序，不写入注册表，不产生垃圾文件。可以放在任何位置运行，或轻松部署到多台电脑。

## 🛠️ 技术实现

本项目从一个简单的 `Robot` 模拟演进而来，最终采用了一套更为底层、稳定且优雅的技术方案：
*   **全局事件监听**: 使用 [JNativeHook](https://github.com/kwhat/jnativehook) 库在系统底层捕获全局键盘事件，保证了即使焦点在远程桌面内也能接收到按键信息。
*   **Windows API 调用**: 通过 [JNA (Java Native Access)](https://github.com/java-native-access/jna) 直接调用 Windows User32 API (`EnumWindows`, `GetWindowText`, `ShowWindow`, `SetForegroundWindow` 等)，实现了对窗口的精确查找、最小化和焦点控制，比模拟按键更加稳定可靠。
*   **健壮的事件处理**: 解决了 `Alt+Tab` 快速操作下的竞态条件问题，确保了功能的高可靠性。
*   **独立的绿色部署**: 使用 JDK 17+ 自带的 `jpackage` 工具进行打包，将应用程序与一个精简的 Java 运行时（JRE）捆绑在一起，生成一个无需外部依赖、免安装的 `app-image`，实现了真正的绿色便携。
*   **后台初始化优化**: 通过设置 `-Djna.tmpdir=%TEMP%` JVM 参数，解决了后台进程因权限问题无法加载本地库的经典难题，确保了程序的静默启动能力。

## 📦 如何构建

本项目使用 [Apache Maven](https://maven.apache.org/) 进行构建和依赖管理，使用 `jpackage` (需要 JDK 14+) 进行最终打包。

**环境要求:**
*   **JDK 17** 或更高版本
*   **Apache Maven**
*   **WiX Toolset v3.x** (用于 `jpackage` 在 Windows 上打包)

**构建步骤:**

1.  **克隆仓库**
    ```bash
    git clone https://github.com/yunqishix/Smart-Local-Alt-Tab-Switcher.git
    cd Smart-Local-Alt-Tab-Switcher
    ```

2.  **使用 Maven 构建 Fat JAR**
    ```bash
    mvn clean package
    ```
    此命令将在 `target` 目录下生成一个包含所有依赖的 `smart-switcher-1.0.0.jar` 文件。

3.  **使用 `jpackage` 创建绿色应用**
    ```bash
    jpackage --name "SmartSwitcher" ^
             --input target/ ^
             --main-jar smart-switcher-1.0.0.jar ^
             --type app-image ^
             --dest build ^
             --java-options "-Djna.tmpdir=%TEMP%"
    ```
    构建成功后，最终的免安装应用程序将位于 `build/SmartSwitcher` 目录下。

## 🚀 如何使用

1.  从 [Releases](https://github.com/YourUsername/Smart-Local-Alt-Tab-Switcher/releases) 页面下载最新的 `SmartSwitcher.zip` 压缩包。
2.  解压到您喜欢的任意位置（例如 `D:\Tools\SmartSwitcher`）。
3.  双击运行 `SmartSwitcher.exe`。程序将在后台静默启动。
4.  (推荐) 设置开机自启，实现一劳用于逸：
    *   右键点击 `SmartSwitcher.exe` -> 发送到 -> 桌面快捷方式。
    *   按 `Win + R`，输入 `shell:startup` 并回车。
    *   将桌面上的快捷方式移动到打开的“启动”文件夹中。

现在，享受在远程桌面中丝滑切换回本地的自由吧！

## 🔧 自定义配置

您可以直接在 `SmartLocalSwitcherV13_Fixed.java` 源码中修改以下常量来适配更多软件或命名规则：
*   `TARGET_WINDOW_KEYWORDS_REGEX`: 定义了所有需要触发本功能的窗口标题关键字，使用 `|` 分隔。
*   `HOSTNAME_PREFIXES`: 定义了哪些前缀被识别为“外壳”窗口，用于内外层逻辑的判断。

修改后，按照“如何构建”的步骤重新打包即可。


## 📄 许可证

本项目采用 [MIT License](LICENSE)。