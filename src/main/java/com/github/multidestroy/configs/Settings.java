package com.github.multidestroy.configs;

import java.util.Random;

public class Settings {

    public final boolean email_authorization;
    public final boolean email_hint;
    public final short login_attempt_time;
    public final boolean ip_blockade;
    public final short same_ip_blockade_low;
    public final short same_ip_blockade_top;
    public final short different_ip_blockade_low;
    public final short different_ip_blockade_top;
    public final boolean slowness;
    public final boolean blindness;
    public final boolean moving_blockade;
    public final boolean session;

    public Settings(Config config) {
        short temp;
        email_authorization = config.getBooleanSetting("settings.e-mail.authorization");
        email_hint = config.getBooleanSetting("settings.e-mail.hint");
        login_attempt_time = (short) config.getIntegerSetting("settings.login_attempt.time");
        ip_blockade = config.getBooleanSetting("settings.login_attempt.ip_blockade.blockade");
        same_ip_blockade_low = (short) config.getIntegerSetting("settings.login_attempt.ip_blockade.tries.same_ip.low");
        same_ip_blockade_top = (temp = (short) config.getIntegerSetting("settings.login_attempt.ip_blockade.tries.same_ip.top"))
                                     < same_ip_blockade_low ? same_ip_blockade_low : temp;
                                     //if low value is bigger than top value, set up top equals to low

        different_ip_blockade_low = (short) config.getIntegerSetting("settings.login_attempt.ip_blockade.tries.different_ip.low");
        different_ip_blockade_top = (temp = (short) config.getIntegerSetting("settings.login_attempt.ip_blockade.tries.different_ip.top"))
                                     < different_ip_blockade_low ? different_ip_blockade_low : temp;
                                     //if low value is bigger than top value, set up top equals to low

        slowness = config.getBooleanSetting("settings.login_attempt.slowness");
        blindness = config.getBooleanSetting("settings.login_attempt.blindness");
        moving_blockade = config.getBooleanSetting("settings.login_attempt.moving_blockade");
        session = config.getBooleanSetting("settings.session");
    }

    /**
     * @return Number placed between lower and upper compartment(low & top) in same_ip setting
     */

    public short getRandomNumberSameIp() {
        return (short) (new Random().nextInt(same_ip_blockade_top - same_ip_blockade_low + 1) + same_ip_blockade_low);
    }

    /**
     * @return Number placed between lower and upper compartment(low & top) in different_ip setting
     */

    public short getRandomNumberDifferentIp() {
        return (short) (new Random().nextInt(different_ip_blockade_top - different_ip_blockade_low + 1) + different_ip_blockade_low);
    }

}
