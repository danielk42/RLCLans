package com.ironchoobs.rlclans.data;

import java.util.HashMap;
import java.util.Map;

public enum WomMetric {
    overall("overall", "Overall"),
    attack("attack", "Attack"),
    defence("defence", "Defence"),
    strength("strength", "Strength"),
    hitpoints("hitpoints", "Hitpoints"),
    ranged("ranged", "Ranged"),
    prayer("prayer", "Prayer"),
    magic("magic", "Magic"),
    cooking("cooking", "Cooking"),
    woodcutting("woodcutting", "Woodcutting"),
    fletching("fletching", "Fletching"),
    crafting("crafting", "Crafting"),
    smithing("smithing", "Smithing"),
    mining("mining", "Mining"),
    herblore("herblore", "Herblore"),
    agility("agility", "Agility"),
    thieving("thieving", "Thieving"),
    slayer("slayer", "Slayer"),
    farming("farming", "Farming"),
    runecrafting("runecrafting", "Runecrafting"),
    hunter("hunter", "Hunter"),
    construction("construction", "Construction"),
    leaguePoints("league_points", "League Points"),
    bountyHunterHunter("bounty_hunter_hunter", "Bounty Hunter (Hunter)"),
    bountyHunterRogue("bounty_hunter_rogue", "Bounty Hunter (Rogue)"),
    clueScrollsAll("clue_scrolls_all", "Clue Scrolls (All)"),
    clueScrollsBeginner("clue_scrolls_beginner", "Beginner Clue Scrolls"),
    clueScrollsEasy("clue_scrolls_easy", "Easy Clue Scrolls"),
    clueScrollsMedium("clue_scrolls_medium", "Medium Clue Scrolls"),
    clueScrollsHard("clue_scrolls_hard", "Hard Clue Scrolls"),
    clueScrollsElite("clue_scrolls_elite", "Elite Clue Scrolls"),
    clueScrollsMaster("clue_scrolls_master", "Master Clue Scrolls"),
    lastManStanding("last_man_standing", "Last Man Standing"),
    soulWarsZeal("soul_wars_zeal", "Soul Wars Zeal"),
    abyssalSire("abyssal_sire", "Abyssal Sire"),
    alchemicalHydra("alchemical_hydra", "Alchemical Hydra"),
    barrowsChests("barrows_chests", "Barrows Chests"),
    bryophyta("bryophyta", "Bryophyta"),
    callisto("callisto", "Callisto"),
    cerberus("cerberus", "Cerberus"),
    chambersOfXeric("chambers_of_xeric", "Chambers of Xeric"),
    chambersOfXericChallengeMode("chambers_of_xeric_challenge_mode", "Chambers of Xeric (CM)"),
    chaosElemental("chaos_elemental", "Chaos Elemental"),
    chaosFanatic("chaos_fanatic", "Chaos Fanatic"),
    commanderZilyana("commander_zilyana", "Commander Zilyana"),
    corporealBeast("corporeal_beast", "Corporeal Beast"),
    crazyArchaeologist("crazy_archaeologist", "Crazy Archaeologist"),
    dagannothPrime("dagannoth_prime", "Dagannoth Prime"),
    dagannothRex("dagannoth_rex", "Dagannoth Rex"),
    dagannothSupreme("dagannoth_supreme", "Dagannoth Supreme"),
    derangedArchaeologist("deranged_archaeologist", "Deranged Archaeologist"),
    generalGraardor("general_graardor", "General Graardor"),
    giantMole("giant_mole", "Giant Mole"),
    grotesqueGuardians("grotesque_guardians", "Grotesque Guardians"),
    hespori("hespori", "Hespori"),
    kalphiteQueen("kalphite_queen", "Kalphite Queen"),
    kingBlackDragon("king_black_dragon", "King Black Dragon"),
    kraken("kraken", "Kraken"),
    kreearra("kreearra", "Kree'Arra"),
    krilTsutsaroth("kril_tsutsaroth", "K'ril Tsutsaroth"),
    mimic("mimic", "Mimic"),
    nex("nex", "Nex"),
    nightmare("nightmare", "Nightmare"),
    phosanisNightmare("phosanis_nightmare", "Phosani's Nightmare"),
    obor("obor", "Obor"),
    sarachnis("sarachnis", "Sarachnis"),
    scorpia("scorpia", "Scorpia"),
    skotizo("skotizo", "Skotizo"),
    tempoross("tempoross", "Tempoross"),
    theGauntlet("the_gauntlet", "The Gauntlet"),
    theCorruptedGauntlet("the_corrupted_gauntlet", "The Corrupted Gauntlet"),
    theatreOfBlood("theatre_of_blood", "Theatre of Blood"),
    theatreOfBloodHardMode("theatre_of_blood_hard_mode", "Theatre of Blood (HM)"),
    thermonuclearSmokeDevil("thermonuclear_smoke_devil", "Thermonuclear Smoke Devil"),
    tzkalZuk("tzkal_zuk", "TzKal-Zuk"),
    tztokJad("tztok_jad", "TzTok-Jad"),
    venenatis("venenatis", "Venenatis"),
    vetion("vetion", "Vet'ion"),
    vorkath("vorkath", "Vorkath"),
    wintertodt("wintertodt", "Wintertodt"),
    zalcano("zalcano", "Zalcano"),
    zulrah("zulrah", "Zulrah"),
    ehp("ehp", "EHP"),
    ehb("ehb", "EHB");

    private static final Map<String, WomMetric> map = new HashMap<>();

    static {
        for (WomMetric m : values()) {
            map.put(m.metric, m);
        }
    }

    final String metric;
    final String displayName;

    WomMetric(String metric, String displayName) {
        this.metric = metric;
        this.displayName = displayName;
    }

    public String getMetric() {
        return this.metric;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static WomMetric toMetric(String metric) {
        return map.get(metric);
    }
}
