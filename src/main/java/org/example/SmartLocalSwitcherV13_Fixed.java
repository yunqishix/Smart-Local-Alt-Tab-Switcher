package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 智能型本地切换器 (V13.1)
 * 1. 统一规则：一个列表同时定义软件名和主机名前缀，简化配置。
 * 2. 智能决策：
 *    - 若触发窗口是软件名，则查找并最小化主机名窗口（内外层模式）。
 *    - 若触发窗口已是主机名，则直接最小化自身（单层模式）。
 * 3. 终极形态：实现了最高的配置简洁性和最强的场景适应性。
 */
public class SmartLocalSwitcherV13_Fixed implements NativeKeyListener {

    // V13 核心升级: 统一所有识别关键字到一个地方
    private static final String TARGET_WINDOW_KEYWORDS_REGEX = "RaysyncDesktop|TeamViewer|向日葵|LAPTOP-|DESKTOP-";

    // 主机名前缀单独定义，用于区分内外层
    private static final String[] HOSTNAME_PREFIXES = {"LAPTOP-", "DESKTOP-"};

    private volatile long lastAltPressTimestamp = 0;
    private static final long ALT_TAB_TIMEOUT_MS = 500;
    private volatile boolean isIntervening = false;

    // JNA 扩展 User32 接口以包含 EnumWindows
    private interface User32Extra extends User32 {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        // 2. 注册全局钩子
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("注册全局钩子失败: " + ex.getMessage());
            System.exit(1);
        }

        // 3. 将我们的监听器实例添加到全局屏幕
        GlobalScreen.addNativeKeyListener(new SmartLocalSwitcherV13_Fixed());
        // --- 初始化代码结束 ---

        System.out.println("智能型本地切换器 (V13.1 - 最终修复版) 已启动。");
        System.out.println("统一匹配规则: " + TARGET_WINDOW_KEYWORDS_REGEX);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            lastAltPressTimestamp = System.currentTimeMillis();
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_TAB && !isIntervening) {
            long timeSinceAltPress = System.currentTimeMillis() - lastAltPressTimestamp;

            if (timeSinceAltPress < ALT_TAB_TIMEOUT_MS) {
                lastAltPressTimestamp = 0; // 消费令牌

                String activeWindowTitle = getActiveWindowTitle();
                if (isTargetWindow(activeWindowTitle)) {
                    isIntervening = true;
                    HWND activeHwnd = User32.INSTANCE.GetForegroundWindow();

                    System.out.println("在目标窗口 [" + activeWindowTitle + "] 检测到有效 Alt+Tab 操作。");

                    // 启动新线程，执行智能决策逻辑
                    new Thread(() -> processSmartSwitch(activeHwnd, activeWindowTitle)).start();
                }
            }
        }
    }

    private void processSmartSwitch(HWND activeHwnd, String activeTitle) {
        HWND windowToMinimize = null;
        if (isHostnameTitle(activeTitle)) {
            System.out.println("决策：当前窗口为外层/单层模式，将直接最小化。");
            windowToMinimize = activeHwnd;
        } else {
            System.out.println("决策：当前窗口为内层模式，开始搜索外层窗口...");
            windowToMinimize = findOuterWindow();
        }
        if (windowToMinimize != null) {
            minimizeAndSetFocus(windowToMinimize);
        } else {
            System.err.println("错误：未能定位到要最小化的目标窗口！");
        }
    }

    private void minimizeAndSetFocus(HWND hwndToMinimize) {
        HWND nextWindowToFocus = findNextVisibleWindow(hwndToMinimize);
        System.out.println("正在最小化目标窗口: [" + getWindowTitle(hwndToMinimize) + "]");
        User32.INSTANCE.ShowWindow(hwndToMinimize, User32.SW_MINIMIZE);

        if (nextWindowToFocus != null) {
            System.out.println("尝试将焦点设置到窗口: [" + getWindowTitle(nextWindowToFocus) + "]");
            try { Thread.sleep(50); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            User32.INSTANCE.SetForegroundWindow(nextWindowToFocus);
            System.out.println("焦点切换操作成功。");
        } else {
            System.err.println("未找到合适的窗口来接收焦点。");
        }
    }

    private HWND findOuterWindow() {
        final HWND[] targetHwnd = {null};
        User32Extra.INSTANCE.EnumWindows((hwnd, pointer) -> {
            if (!User32.INSTANCE.IsWindowVisible(hwnd)) return true;
            String windowTitle = getWindowTitle(hwnd);
            if (isHostnameTitle(windowTitle)) {
                System.out.println("找到外层窗口: [" + windowTitle + "]");
                targetHwnd[0] = hwnd;
                return false;
            }
            return true;
        }, null);
        return targetHwnd[0];
    }

    private boolean isHostnameTitle(String windowTitle) {
        if (windowTitle == null || windowTitle.isEmpty()) return false;
        String upperTitle = windowTitle.toUpperCase();
        for (String prefix : HOSTNAME_PREFIXES) {
            if (upperTitle.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetWindow(String windowTitle) {
        if (windowTitle == null || windowTitle.isEmpty()) return false;
        return windowTitle.matches(".*(" + TARGET_WINDOW_KEYWORDS_REGEX + ").*");
    }

    private HWND findNextVisibleWindow(HWND startHwnd) {
        HWND currentHwnd = startHwnd;
        while (true) {
            currentHwnd = User32.INSTANCE.GetWindow(currentHwnd, new DWORD(User32.GW_HWNDNEXT));
            if (currentHwnd == null) return null;
            if (User32.INSTANCE.IsWindowVisible(currentHwnd) && User32.INSTANCE.IsWindowEnabled(currentHwnd)) {
                if (!getWindowTitle(currentHwnd).isEmpty()) {
                    return currentHwnd;
                }
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            if (isIntervening) {
                isIntervening = false;
                System.out.println("Alt 键已释放，重置干预锁。");
            }
        }
    }

    private String getActiveWindowTitle() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        return getWindowTitle(hwnd);
    }

    private String getWindowTitle(HWND hwnd) {
        if (hwnd == null) return "";
        char[] buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer);
    }
}