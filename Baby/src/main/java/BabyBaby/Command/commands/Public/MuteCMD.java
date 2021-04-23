package BabyBaby.Command.commands.Public;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import BabyBaby.Command.CommandContext;
import BabyBaby.Command.PublicCMD;
import BabyBaby.Command.StandardHelp;
import BabyBaby.data.data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class MuteCMD implements PublicCMD {
    public static HashMap<Member, ScheduledExecutorService> userMuted = new HashMap<>();
    public static HashMap<ScheduledExecutorService, GetUnmute> variables = new HashMap<>();

    @Override
    public void handleAdmin(CommandContext ctx) {
        ctx.getChannel().sendMessage("This feature is disabled for Admins because you cant use it anyway..... <:kekwait:786877342072832020>").queue();
    }

    @Override
    public MessageEmbed getAdminHelp(String prefix) {
        return getPublicHelp(prefix);
    }

    @Override
    public void handleOwner(CommandContext ctx) {
        handlePublic(ctx);
    }

    @Override
    public MessageEmbed getOwnerHelp(String prefix) {
        return getPublicHelp(prefix);
    }

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public void handlePublic(CommandContext ctx) {

        if(!ctx.getGuild().equals(data.ethid)){
            return;
        }

        MessageChannel channel = ctx.getChannel();
        Guild called = ctx.getGuild();

        List<String> cmds = ctx.getArgs();
        String number = cmds.get(0);
		
        double time;
        String sunit;
        User muteUser = ctx.getAuthor(); 
        ScheduledExecutorService mute = Executors.newScheduledThreadPool(1);
        
        if(cmds.size() == 0){
            channel.sendMessage("The command is +mute <time> [unit] (default unit is minutes)").queue();
            return;
        }

        try{
        if(number.length() > 18 || Double.parseDouble(number) > Integer.MAX_VALUE){
            time=Integer.MAX_VALUE;
        } else {
                time = Double.parseDouble(number);
        }
        } catch (NumberFormatException e){
            channel.sendMessage("You probably forgot the space between the time and unit, if not use numbers pls!").queue();
            return;
        }

        if(time <= 0){
            channel.sendMessage("Use positive numbers thx!").queue();
            return;
        }			


        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        //LocalDateTime now = LocalDateTime.now(); 
        
        List<Role> stfuroles = called.getRolesByName("STFU", true); 
        Role muteR = stfuroles.get(0);

        long rounder = 0;
        
        if(cmds.size() < 2) {
            sunit = "minutes";
            rounder = (long) (time*60);
        } else {
            String timeunit = cmds.get(1);
            timeunit = timeunit.toLowerCase();
            if (timeunit.startsWith("h")){
                sunit = "hours";
                rounder = (long) (time*3600);
            } else if(timeunit.startsWith("m")){
                sunit = "minutes";
                rounder = (long) (time*60);
            } else if(timeunit.startsWith("d")){
                sunit = "days";
                rounder = (long) (time*24*3600);
            } else {
                sunit = "seconds";
                rounder = (long) (time);
            }
        }

        if(rounder <= 0){
            channel.sendMessage("Use numbers above 1 second pls!").queue();
            return;
        }

        
        Connection c = null;
        Statement stmt = null;
        long timesql = (System.currentTimeMillis() + rounder*1000);
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:testone.db");
            
             
            stmt = c.createStatement();           

            String sql = "INSERT INTO USERS (ID, GUILDUSER, MUTETIME) VALUES (" + 
                            ctx.getAuthor().getId() + ", " + called.getId() + ", '"
                            +  timesql + "');"; 
            stmt.executeUpdate(sql);

            stmt.close();
            c.close();
            
        } catch ( Exception e ) {
            channel.sendMessage( e.getClass().getName() + ": " + e.getMessage()).queue();
            e.printStackTrace(); 
            return;
        }


        GetUnmute scheduledclass = new GetUnmute(muteUser, called, muteR);
        mute.schedule(scheduledclass, rounder , TimeUnit.SECONDS);

        userMuted.put(ctx.getMember(), mute);
        variables.put(mute, scheduledclass);

        called.addRoleToMember(ctx.getMember(), muteR).queue();
        channel.sendMessage("You got muted for ~" + time + " " + sunit + ". Either wait out the timer or write me (<@781949572103536650>) in Private chat \"+unmute\"").queue();

    }

    @Override
    public MessageEmbed getPublicHelp(String prefix) {
        return StandardHelp.Help(prefix, getName(), "<time> [unit]", "Mute yourself to learn better.");
    }
    
}