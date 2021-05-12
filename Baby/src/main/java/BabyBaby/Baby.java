package BabyBaby; 

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import javax.security.auth.login.LoginException;

import BabyBaby.data.data;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;




public class Baby {
	
    public static void main(String[] args) throws IOException  {
        
        try {
            String token = "";
            
            Scanner s = new Scanner(new File(data.TOKEN));
            token = s.nextLine();
            s.close();
            
            JDABuilder builder = JDABuilder.createDefault(token);
            builder.enableIntents(
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_VOICE_STATES);
            builder.setChunkingFilter(ChunkingFilter.ALL);
            
            JDA jda = builder.build();
            jda.addEventListener(new BabyListener(jda));
            //jda.addEventListener(new MyListener());
            jda.getPresence().setActivity(Activity.watching(" some pixels getting slowly placed."));
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }
}