package BabyBaby.Listeners;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import BabyBaby.Command.commands.Admin.*;
import BabyBaby.Command.commands.Public.*;
import BabyBaby.data.GetRolesBack;
import BabyBaby.data.GetUnmute;
import BabyBaby.data.data;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class StartupListener extends ListenerAdapter{
    
    public final JDA bot;

    public StartupListener(JDA bot) throws IOException {
        this.bot = bot;
    }


    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        long timestopper = System.currentTimeMillis();


        LinkedList<Thread> threads = new LinkedList<>();
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                Connection c = null;
                Statement stmt = null;

                ResultSet rs;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    
                    stmt = c.createStatement();

                    rs = stmt.executeQuery("SELECT * FROM GUILD;");
                    
                    while (rs.next()) {
                        String id = rs.getString("ID");
                        String prefixstr = rs.getString("PREFIX");
                        BabyListener.prefix.put(id, prefixstr);
                    }
                    rs.close();
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    System.out.println(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }));
        threads.getLast().start();

        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;

                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    
                    stmt = c.createStatement();

                    rs = stmt.executeQuery("SELECT * FROM USERS");            
                    while(rs.next()){
                        try{
                        Guild called = bot.getGuildById( rs.getString("GUILDUSER"));
                        User muteUser = called.getMemberById(rs.getString("ID")).getUser();
                        long time = Long.parseLong(rs.getString("MUTETIME"));
                        ScheduledExecutorService mute = Executors.newScheduledThreadPool(1);
                        List<Role> tmp = called.getRolesByName("STFU", true);
                        Role muteR = tmp.get(0);
                        GetUnmute muteclass = new GetUnmute(muteUser, called, muteR);
                        mute.schedule(muteclass, (time-System.currentTimeMillis())/1000, TimeUnit.SECONDS);
                        MuteCMD.userMuted.put(called.getMember(muteUser), mute);
                        } catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Probably a User that left the server while being muted.");
                        }

                    }
                    stmt.close();
                    c.close();

                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();

        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM ASSIGNROLES;");

                    while(rs.next()){
                        data.emoteassign.put(rs.getString("EMOTE"), rs.getString("ID"));
                    }

                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();


        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM MSGS;");
                    while(rs.next()){
                        data.msgid.add(rs.getString("MSGID"));
                    }
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();

        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                Connection c = null;
                List<Invite> inv = event.getJDA().getGuildById(data.ethid).retrieveInvites().complete();
                HashMap<String, Invite> urls = new HashMap<>();
                for (Invite var : inv) {
                    urls.put(var.getUrl(), var);
                }

                c = null;
                PreparedStatement pstmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    pstmt = c.prepareStatement("DELETE FROM INVITES;");
                    pstmt.execute();
                    pstmt.close();
                    c.close();
                } catch ( Exception e ) {
                    System.out.println(e.getClass().getName() + ": " + e.getMessage());
                }

                c = null;
                pstmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    for (String var : urls.keySet()) {
                        pstmt = c.prepareStatement("INSERT INTO INVITES (URL, AMOUNT) VALUES (?, ?);");
                        pstmt.setString(1, var);
                        pstmt.setInt(2, urls.get(var).getUses());
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                    c.close();
                } catch ( Exception e ) {
                    System.out.println(e.getClass().getName() + ": " + e.getMessage());
                } 
            }
        }));
        threads.getLast().start();


        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                AuditLogPaginationAction logs = event.getJDA().getGuildById(data.ethid).retrieveAuditLogs();
                for (AuditLogEntry entry : logs) {
                    if(entry.getType().equals(ActionType.KICK)){
                        data.kick = entry.getTimeCreated();
                        break;
                    }   
                }
                for (AuditLogEntry entry : logs) {
                    if(entry.getType().equals(ActionType.BAN)){
                        data.ban = entry.getTimeCreated();
                        break;
                    }   
                }
            }
        }));
        threads.getLast().start();


        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM REMINDERS;");

                    while(rs.next()){
                        try {
                        ScheduledExecutorService remind = Executors.newScheduledThreadPool(1);
                        remind.schedule(new GetReminder(event.getJDA().getUserById(rs.getString("USERID")), event.getJDA().getGuildById(rs.getString("GUILDID")), rs.getString("TEXTS"), rs.getString("CHANNELID"), rs.getString("pk")), (rs.getLong("TIME")-System.currentTimeMillis())/1000 , TimeUnit.SECONDS);
                        } catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Probably a User that left the server while being reminded.");
                        }
                    }
                    
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();


        //Put admin mutes in cache
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM ADMINMUTE;");

                    while(rs.next()){
                        try{
                            Guild muteG = event.getJDA().getGuildById(rs.getString("GUILDID"));
                            User mutedPerson = event.getJDA().getUserById(rs.getString("USERID"));
                            int time = rs.getInt("TIME");
                            if(time == 0){
                                MutePersonCMD.userMuted.put(muteG.getMember(mutedPerson), null);
                            } else {
                                ScheduledExecutorService mute = Executors.newScheduledThreadPool(1);
                                mute.schedule(new GetUnmutePerson(mutedPerson, muteG), time , TimeUnit.SECONDS);
                                MutePersonCMD.userMuted.put(muteG.getMember(mutedPerson), mute);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Probably a User that left the server while being adminmuted.");
                        } 
                    }
                    
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();



        //put assign message ids in cache
        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;
                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    stmt = c.createStatement();
                    rs = stmt.executeQuery("SELECT MSGID FROM MSGS;");
        
                    while(rs.next()){
                        data.msgid.add(rs.getString("MSGID"));
                    }
                    
                    stmt.close();
                    c.close();
                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();

        threads.add(new Thread(new Runnable() {
            @Override
            public void run() {
                ResultSet rs;
                Connection c = null;
                Statement stmt = null;

                try {
                    Class.forName("org.sqlite.JDBC");
                    c = DriverManager.getConnection(data.db);
                    
                    stmt = c.createStatement();

                    rs = stmt.executeQuery("SELECT * FROM ROLEREMOVAL");            
                    while(rs.next()){
                        try{
                        Guild called = bot.getGuildById( rs.getString("GUILDID"));
                        User blindUser = called.getMemberById(rs.getString("USERID")).getUser();
                        long time = Long.parseLong(rs.getString("MUTETIME"));
                        ScheduledExecutorService blindex = Executors.newScheduledThreadPool(1);
                        
                        GetRolesBack blindclass = new GetRolesBack(blindUser, called, rs.getString("ROLES"));
                        blindex.schedule(blindclass, (time-System.currentTimeMillis())/1000, TimeUnit.SECONDS);
                        RemoveRoles.blind.put(called.getMember(blindUser), blindex);
                        RemoveRoles.blindexe.put(blindex, blindclass);
                        
                        if(rs.getBoolean("ADMINMUTE")){
                            AdminMuteBlindCMD.userBlinded.add(called.getMember(blindUser));
                        }
                        
                        } catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Probably a User that left the server while being blinded.");
                        }

                    }
                    stmt.close();
                    c.close();

                } catch ( Exception e ) {
                    e.printStackTrace(); 
                }
            }
        }));
        threads.getLast().start();

        for (Thread var : threads) {
            try{
                var.join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
        System.out.println("Started!" + (System.currentTimeMillis()-timestopper));
    }

}