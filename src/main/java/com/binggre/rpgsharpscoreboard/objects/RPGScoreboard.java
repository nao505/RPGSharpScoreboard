package com.binggre.rpgsharpscoreboard.objects;

import com.binggre.rpgsharpscoreboard.RPGSharpScoreboard;
import com.binggre.rpgsharpskillplus.objects.PlayerSkillPlusLoader;
import com.google.gson.JsonObject;
import com.hj.rpgsharp.rpg.apis.rpgsharp.RPGSharpAPI;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.FileUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.HexColorUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.ItemUtil;
import com.hj.rpgsharp.rpg.apis.rpgsharp.utils.NumberUtil;
import com.hj.rpgsharp.rpg.objects.RPGBuff;
import com.hj.rpgsharp.rpg.objects.RPGPlayer;
import com.hj.rpgsharp.rpg.plugins.party.objects.Party;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPGScoreboard {

    private static final RPGScoreboard instance = new RPGScoreboard();

    public static RPGScoreboard getInstance() {
        return instance;
    }

    public final String CRITERIA_NAME;
    private final Scoreboard CLEAR;
    private final Map<String, Scoreboard> SCOREBOARD;
    private final String DELETE_KEY;

    private String TITLE;
    private String BUFF_PLACEHOLDER;
    private String PARTY_TITLE;
    private String PARTY_PLACEHOLDER;
    private List<String> TEXT_CONTENTS;

    private RPGScoreboard() {
        this.CLEAR = createNewScoreboard();
        this.SCOREBOARD = new HashMap<>();
        this.TITLE = null;
        this.BUFF_PLACEHOLDER = null;
        this.TEXT_CONTENTS = new ArrayList<>();
        this.CRITERIA_NAME = "RPGSharpScoreboard";
        this.DELETE_KEY = "RPGSharpScoreboard_Delete_Key";
    }

    public Map<String, Scoreboard> getScoreboard() {
        return SCOREBOARD;
    }

    public void update(RPGPlayer rpgPlayer) {
        final String NICKNAME = rpgPlayer.getNickname();
        final Player PLAYER = rpgPlayer.toPlayer();
        PLAYER.setScoreboard(CLEAR);
        Objective objective = createObjective(NICKNAME);
        setDisplayName(rpgPlayer, objective);
        reloadObjective(rpgPlayer, objective);
        PLAYER.setScoreboard(objective.getScoreboard());
    }

    public Scoreboard createNewScoreboard() {
        return Bukkit.getScoreboardManager().getNewScoreboard();
    }

    private Objective createObjective(String nickname) {
        Scoreboard scoreboard = getScoreboard().get(nickname);
        if (scoreboard.getObjective(nickname) != null) {
            scoreboard.getObjective(nickname).unregister();
        }
        return scoreboard.registerNewObjective(nickname, CRITERIA_NAME);
    }

    private void setDisplayName(RPGPlayer rpgPlayer, Objective objective) {
        objective.setDisplayName(TITLE.replace("<player>", rpgPlayer.getNickname()));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void reloadObjective(RPGPlayer rpgPlayer, Objective objective) {
        List<String> copyTextContents = new ArrayList<>(TEXT_CONTENTS);
        setContents(rpgPlayer, copyTextContents);
        int line = copyTextContents.size();
        for (String str : copyTextContents) {
            if (str.equals(DELETE_KEY)) {
                continue;
            }
            objective.getScore(str).setScore(line);
            line--;
        }
    }

    private void setContents(RPGPlayer rpgPlayer, List<String> textContents) {
        int line = -1;

        if (rpgPlayer.getRPGBuff().hasBuff()) {
            RPGBuff rpgBuff = rpgPlayer.getRPGBuff();
            List<String> buffs = rpgBuff.getBuffs();
            String buffName;
            int buffCooldown;
            String str;

            textContents.add("");
            for (String dataCode : buffs) {
                buffName = RPGSharpAPI.getRPGItemAPI().getRPGItem(dataCode).getFakeItem().getDisplayName();
                buffCooldown = rpgBuff.getBuffDuration(dataCode);
                str = BUFF_PLACEHOLDER
                        .replace("<buff_name>", buffName)
                        .replace("<buff_cooldown>", buffCooldown + "");
                textContents.add(str);
            }
        }
        party:
        if (RPGSharpAPI.getPartyAPI().isJoin(rpgPlayer)) {
            Party party = RPGSharpAPI.getPartyAPI().getParty(rpgPlayer);
            if (party.getMembers().size() == 1) {
                break party;
            }
            textContents.add("");
            textContents.add(PARTY_TITLE.replace("<player>", RPGSharpAPI.getRPGPlayerAPI().getRPGPlayer(party.getLeader()).getNickname()));
            String nickname;
            String health;
            String distance;
            String str;
            Player player = rpgPlayer.toPlayer();
            for (Player member : party.getPlayers()) {
                if (member == player) {
                    continue;
                }
                nickname = member.getName();
                health = NumberUtil.decimalFormat(NumberUtil.getPercentage(member.getHealth(), member.getMaxHealth()), 1);
                distance = (member.getWorld() == player.getWorld()) ?
                        NumberUtil.decimalFormat(member.getLocation().distance(player.getLocation()), 2)
                        :
                        "?";
                str = PARTY_PLACEHOLDER
                        .replace("<party_member>", nickname)
                        .replace("<party_member_health>", health)
                        .replace("<party_member_distance>", distance);
                textContents.add(str);
            }
        }

        for (String str : textContents) {
            line++;
            if (str.contains("<nickname>")) {
                str = replace(str, "<nickname>", rpgPlayer.getNickname());
            }
            if (str.contains("<account_nickname>")) {
                str = replace(str, "<account_nickname>", rpgPlayer.getDefaultName());
            }
            if (str.contains("<job>")) {
                str = replace(str, "<job>", rpgPlayer.getJob(true));
            }
            if (str.contains("<title>")) {
                str = (rpgPlayer.getRPGTitle().isEquip()) ?
                        replace(str, "<title>", rpgPlayer.getRPGTitle().getTitle())
                        :
                        replace(str, "<title>", "없음");
            }
            if (str.contains("<level>")) {
                str = replace(str, "<level>", rpgPlayer.getLevel() + "");
            }
            if (str.contains("<exp>")) {
                double exp = NumberUtil.getPercentage(rpgPlayer.getExp(), rpgPlayer.getMaxExp());
                str = replace(str, "<exp>", NumberUtil.decimalFormat(exp, 1));
            }
            if (str.contains("<stat_point>")) {
                str = replace(str, "<stat_point>", rpgPlayer.getStatPoint() + "");
            }
            if (str.contains("<skill_point>") && isEnablePlugin("RPGSharpSkillPlus")) {
                str = (isEnablePlugin("RPGSharpSkillPlus")) ?
                        replace(str, "<skill_point>", PlayerSkillPlusLoader.get(rpgPlayer).getSkillPoint() + "")
                        :
                        DELETE_KEY;
            }
            textContents.set(line, str);
        }

    }

    private String replace(String value, String oldVar, String newVar) {
        return value.replace(oldVar, newVar);
    }

    private boolean isEnablePlugin(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    public void loadJson() {
        final String FILE_NAME = "scoreboard";
        final String FILE_FULL_NAME = FILE_NAME + ".json";
        final String FILE_PATH = RPGSharpAPI.getDirectory() + "\\" + FILE_FULL_NAME;
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            FileUtil.moveFile(RPGSharpScoreboard.class.getResourceAsStream("/" + FILE_FULL_NAME), FILE_PATH, false);
        }
        JsonObject json = FileUtil.read(file);
        assert (json != null);

        List<String> textContents = FileUtil.getStringList(json.get("내용").getAsJsonArray());
        ItemUtil.colored(textContents);

        TITLE = HexColorUtil.format(json.get("제목").getAsString().replace("&", "§"));
        PARTY_TITLE = HexColorUtil.format(json.getAsJsonObject("파티").get("제목").getAsString().replace("&", "§"));
        PARTY_PLACEHOLDER = HexColorUtil.format(json.getAsJsonObject("파티").get("내용").getAsString().replace("&", "§"));
        BUFF_PLACEHOLDER = HexColorUtil.format(json.get("버프").getAsString().replace("&", "§"));
        TEXT_CONTENTS = textContents;
    }
}