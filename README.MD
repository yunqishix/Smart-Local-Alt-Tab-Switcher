# Smart Local Alt-Tab Switcher

<p align="center">
  <a href="README.md">English</a> | <a href="README_zh.md">简体中文</a>
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**A tiny Windows utility designed for remote desktop users, solving the pain point where `Alt+Tab` gets trapped in the remote session, preventing you from switching back to your local machine. It provides a seamless, "invisible" local window switching experience.**

---

## 🧐 The Problem

When you are using remote desktop software like TeamViewer, AnyDesk, or Raysync in full-screen mode on Windows, you'll notice that the `Alt+Tab` shortcut is captured by the remote system. This means you can't use this familiar hotkey to quickly switch back to your local applications (e.g., your browser, chat client, or code editor). You are forced to use cumbersome methods like `Ctrl+Alt+Delete` to exit full-screen mode, which severely disrupts your workflow and efficiency.

## ✨ The Solution

**Smart Local Alt-Tab Switcher** runs silently in the background, intelligently listening for keyboard events. When it detects that you've pressed `Alt+Tab` within a targeted remote desktop window, it cleverly bypasses the remote capture and performs one core action: **it minimizes the main window of the remote desktop session.**

The effect of this simple action is revolutionary:
*   **Instant Switching**: As the remote window is minimized, your last active local window is immediately revealed.
*   **Automatic Focus Transfer**: The keyboard focus naturally shifts to the local window, allowing you to start working instantly.
*   **Seamless Experience**: The entire process feels just like using the native `Alt+Tab`, making the tool's presence almost imperceptible.

## 🚀 Features

*   **🎯 Smart Detection**: Automatically identifies various mainstream remote desktop applications (e.g., Raysync, TeamViewer) and windows titled with standard computer names (e.g., `LAPTOP-XXXX`, `DESKTOP-XXXX`).
*   **🧠 Intelligent Decision-Making**: Capable of handling the "inner/outer shell" window structure of remote software. It accurately targets and minimizes the correct window, whether you are interacting with the inner remote screen or its outer container.
*   **⚡️ High Sensitivity & Fault Tolerance**: Utilizes a timestamp-based event handling mechanism to accurately capture your `Alt+Tab` intent, even if you press and release the `Alt` key extremely quickly, preventing misfires.
*   **🤫 Completely Silent & "Invisible"**: The program runs in the background with minimal resource consumption, without any visible windows or icons. Set it up once, let it start with Windows, and forget it's even there.
*   **Portable & Green**: Packaged as a standalone application that doesn't require installation, write to the registry, or leave junk files. Run it from anywhere or easily deploy it across multiple machines.

## 🛠️ Technical Implementation

This project evolved from a simple `Robot`-based simulation to a more robust, stable, and elegant low-level solution:
*   **Global Event Listening**: Uses [JNativeHook](https://github.com/kwhat/jnativehook) to capture global keyboard events at the system level, ensuring key presses are received even when the focus is inside the remote session.
*   **Windows API Calls**: Leverages [JNA (Java Native Access)](https://github.com/java-native-access/jna) to directly call Windows User32 APIs (`EnumWindows`, `GetWindowText`, `ShowWindow`, `SetForegroundWindow`, etc.) for precise window finding, minimization, and focus control, which is far more reliable than simulating keystrokes.
*   **Robust Event Handling**: Solves the race condition issue inherent in fast `Alt+Tab` operations, ensuring high reliability.
*   **Standalone Green Deployment**: Uses the `jpackage` tool (included in JDK 17+) to bundle the application with a minimal Java Runtime (JRE), creating a dependency-free, portable "app-image".
*   **Optimized Background Initialization**: Addresses the classic issue of background processes failing to load native libraries due to permission errors by setting the `-Djna.tmpdir=%TEMP%` JVM property, enabling silent startup.

## 📦 How to Build

This project uses [Apache Maven](https://maven.apache.org/) for dependency management and `jpackage` (requires JDK 14+) for final packaging.

**Prerequisites:**
*   **JDK 17** or newer
*   **Apache Maven**
*   **WiX Toolset v3.x** (required by `jpackage` on Windows)

**Build Steps:**
1.  **Clone the repository**
    ```bash
    git clone https://github.com/yunqishix/Smart-Local-Alt-Tab-Switcher.git
    cd Smart-Local-Alt-Tab-Switcher
    ```
2.  **Build the Fat JAR with Maven**
    ```bash
    mvn clean package
    ```
    This command will produce a self-contained JAR file (e.g., `smart-switcher-1.0.0.jar`) in the `target` directory.
3.  **Create the application image with `jpackage`**
    ```bash
    jpackage --name "SmartSwitcher" ^
             --input target/ ^
             --main-jar smart-switcher-1.0.0.jar ^
             --type app-image ^
             --dest build ^
             --java-options "-Djna.tmpdir=%TEMP%"
    ```
    After a successful build, the standalone application will be located in the `build/SmartSwitcher` directory.

## 🚀 How to Use

1.  Download the latest `SmartSwitcher.zip` from the [Releases](https://github.com/YourUsername/Smart-Local-Alt-Tab-Switcher/releases) page.
2.  Extract it to any location you prefer (e.g., `D:\Tools\SmartSwitcher`).
3.  Double-click `SmartSwitcher.exe` to run it. The program will start silently in the background.
4.  (Recommended) Set it to start automatically with Windows for a truly "set it and forget it" experience:
    *   Right-click `SmartSwitcher.exe` -> Send to -> Desktop (create shortcut).
    *   Press `Win + R`, type `shell:startup`, and press Enter.
    *   Move the newly created shortcut from your desktop into the "Startup" folder that just opened.

Now, enjoy the freedom of seamlessly switching back to your local machine from within your remote session!

## 🔧 Configuration

You can customize the application's behavior by modifying the constants in the `SmartLocalSwitcherV13_Fixed.java` source file:
*   `TARGET_WINDOW_KEYWORDS_REGEX`: A regex string defining the keywords (separated by `|`) in window titles that should trigger the tool.
*   `HOSTNAME_PREFIXES`: An array of prefixes used to identify "outer shell" windows for the inner/outer logic.

After making changes, rebuild the project following the "How to Build" steps.


## 📄 License

This project is licensed under the [MIT License](LICENSE).