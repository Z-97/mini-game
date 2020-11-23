package org.chl.common.core.config;

/**
 * @Description
 * @Author mice
 * @Date 2019/10/8 14:27
 * @Version V1.0
 **/
public class RedisRegionConfig {

    // 场次库存
    public static final String FILE_WIN_GOLD_KEY = "file_win_gold";

    // 下注值
    public static final String FILE_BET_GOLD_KEY = "file_bet_gold";

    // 赔付值
    public static final String FILE_PAYOUT_GOLD_KEY = "file_payout_gold";

    // 玩家数量
    public static final String FILE_PLAYER_NUM_KEY = "file_player_num_key";

    // 机器人数量
    public static final String FILE_ROBOT_NUM_KEY = "file_robot_num_key";

    // 房间数量
    public static final String FILE_ROOM_NUM_KEY = "file_room_num_key";

    // 场次抽水
    public static final String FILE_PUMP_KEY = "file_pump";

    // 更新 百人游戏配置
    public static final String UPDATE_BAIREN_GAME_CONFIG_CHANNEL = "update_bairen_game_config_key";

    // 更新老虎机游戏配置
    public static final String UPDATE_LHJ_GAME_CONFIG_CHANNEL = "update_lhj_game_config_key";

    // 汇率配置
    public static final String GAME_EXCHANGE_RATE = "game_exchange_rate";

    // 用户信息
    public static final String USER = "lobby_user";

    //游戏场次信息
    public static final String GAME_FILE_INFO = "game_file_info";

    //游戏实时在线场次信息
    public static final String ONLINE_GAME_FILE_INFO = "online_game_file_info";

    //在线人数
    public static final String ONLINE_NUM_KEY = "file_online_num";

    //用户白名单设备编码
    public static final String USER_WHITE_DEVICE_CODE = "user_white_device_code";

}