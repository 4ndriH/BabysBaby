package BabyBaby.Command.commands.Admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import BabyBaby.Command.AdminCMD;
import BabyBaby.Command.CommandContext;
import BabyBaby.Command.StandardHelp;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class updaterole implements AdminCMD {

    @Override
    public void handleOwner(CommandContext ctx) {
       handleAdmin(ctx);
    }

    @Override
    public MessageEmbed getOwnerHelp(String prefix) {
        return getAdminHelp(prefix);
    }

    @Override
    public String getName() {
        return "updaterole";
    }

    @Override
    public void handleAdmin(CommandContext ctx) {
        List<String> cmds2 = ctx.getArgs();
        MessageChannel channel = ctx.getChannel();
        Connection c = null;
        Statement stmt = null;

        LinkedList<String> cmds = new LinkedList<>();

        for (String var : cmds2) {
            cmds.add(var);
        }

        String casing = cmds.remove(0);
        String id = cmds.remove(0);
        String update = "";
        for (String var : cmds) {
            update += var + " "; 
        }
        update = update.substring(0, update.length()-1);

        //ASSIGNROLES ID categories emote

        switch(casing){
            case "emote":
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:testone.db");
        
                stmt = c.createStatement();
                String sql= "UPDATE ASSIGNROLES SET EMOTE = '" + update + "' where ID=" + id + ";";
                stmt.executeUpdate(sql); 
                
                stmt.close();
                c.close();
            } catch ( Exception e ) {
                channel.sendMessage(e.getClass().getName() + ": " + e.getMessage()).queue();
                return;
            }


                break;
            
            case "id":
                
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection("jdbc:sqlite:testone.db");
            
                    stmt = c.createStatement();
                    String sql = "UPDATE ASSIGNROLES SET ID = " + update + " where ID=" + id + ";";
                    stmt.executeUpdate(sql);
            
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    channel.sendMessage(e.getClass().getName() + ": " + e.getMessage()).queue();
                    return;
                }


                break;
            
            case "category":
                
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection("jdbc:sqlite:testone.db");
            
                    stmt = c.createStatement();
                    String sql = "UPDATE ASSIGNROLES SET categories = '" + update + "' where ID=" + id + ";";
                    stmt.executeUpdate(sql);
            
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    channel.sendMessage(e.getClass().getName() + ": " + e.getMessage()).queue();
                    return;
                }
                
                break;
            default:
                ctx.getMessage().addReaction(":xmark:769279807916998728").queue();
                return;
        }	
    
        ctx.getMessage().addReaction(":checkmark:769279808244809798").queue();
        
    }

    @Override
    public MessageEmbed getAdminHelp(String prefix) {
        return StandardHelp.Help(prefix, getName(), "<id/categories/emote> <roleID atm> <new emote/id/...>", "Command to update the assignable roles if a emote or something changed.");
    }
    
}