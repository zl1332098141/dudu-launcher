package com.wow.carlauncher.ex.manage.baiduVoice.bean;

import com.baidu.speech.asr.SpeechConstant;
import com.google.gson.annotations.SerializedName;

public class AsrStart {

    @SerializedName(SpeechConstant.PID)
    private int pid = 15363;

    @SerializedName(SpeechConstant.ACCEPT_AUDIO_VOLUME)
    private boolean acceptAudioVolume = false;

    @SerializedName(SpeechConstant.NLU)
    private String nul = "enable";

    @SerializedName(SpeechConstant.VAD_ENDPOINT_TIMEOUT)
    private int vadEndpointTimeout = 0;


    @SerializedName(SpeechConstant.VAD)
    private String vad = SpeechConstant.VAD_DNN;

    public boolean isAcceptAudioVolume() {
        return acceptAudioVolume;
    }

    public AsrStart setAcceptAudioVolume(boolean acceptAudioVolume) {
        this.acceptAudioVolume = acceptAudioVolume;
        return this;
    }

    public String getNul() {
        return nul;
    }

    public AsrStart setNul(String nul) {
        this.nul = nul;
        return this;
    }

    public int getVadEndpointTimeout() {
        return vadEndpointTimeout;
    }

    public AsrStart setVadEndpointTimeout(int vadEndpointTimeout) {
        this.vadEndpointTimeout = vadEndpointTimeout;
        return this;
    }

    public String getVad() {
        return vad;
    }

    public AsrStart setVad(String vad) {
        this.vad = vad;
        return this;
    }

    public int getPid() {
        return pid;
    }

    public AsrStart setPid(int pid) {
        this.pid = pid;
        return this;
    }
}
