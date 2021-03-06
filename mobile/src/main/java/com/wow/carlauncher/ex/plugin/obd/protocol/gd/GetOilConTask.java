package com.wow.carlauncher.ex.plugin.obd.protocol.gd;

import static com.wow.carlauncher.ex.plugin.obd.protocol.gd.CommonCmd.CMD_RES_NO_DATA;

/**
 * Created by 10124 on 2018/4/20.
 */

public class GetOilConTask extends GdTask {
    private static final String CMD_REQ = "012F";//2F
    private static final String CMD_RES = "412F";

    private int oil = 0;

    public int getOil() {
        return oil;
    }

    @Override
    public String getReqMessage() {
        return CMD_REQ;
    }

    @Override
    public void writeRes2(String msg) {
        if (msg.startsWith(CMD_RES)) {
            if (msg.length() >= 6) {
                oil = (int) (Integer.parseInt(msg.substring(4, 6), 16) / 256f * 100);
                markSuccess();
            }
        } else if (msg.startsWith(CMD_RES_NO_DATA)) {
            markSuccess();
            markNoData();
        }
    }
}
