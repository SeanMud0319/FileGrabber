package com.nontage.filegrabber;

import com.nontage.filegrabber.command.DiskCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;

public final class FileGrabber extends JavaPlugin {

    private JDA jda;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Enable Plugin");
        saveDefaultConfig();
        String targetServerId = getConfig().getString("server_id");
        String targetChannelId = getConfig().getString("channel_id");
        String token = getConfig().getString("token");
        if(token == null) {
            System.out.println("The bot token is null");
            return;
        }
        if(targetServerId == null) {
            System.out.println("The server Id is null");
            return;
        }
        if(targetChannelId == null) {
            System.out.println("The channel Id is null");
            return;
        }

        JDABuilder builder = JDABuilder.createDefault(token);
        jda = builder.build();

        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onReady(ReadyEvent event) {
                TextChannel textChannel = event.getJDA().getTextChannelById(targetChannelId);
                if (textChannel != null) {
                    DiskCommand diskCommand = new DiskCommand(FileGrabber.this, textChannel);
                    Bukkit.getPluginCommand("disks").setExecutor(diskCommand);
                } else {
                    getLogger().warning("Target text channel not found!");
                }
            }
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disable Plugin");
        if (jda != null) {
            jda.shutdownNow(); // 關閉JDA
        }
    }
}
