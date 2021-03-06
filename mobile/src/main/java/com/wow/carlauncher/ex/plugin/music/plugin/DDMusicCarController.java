package com.wow.carlauncher.ex.plugin.music.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.wow.carlauncher.common.LrcAnalyze;
import com.wow.carlauncher.common.TaskExecutor;
import com.wow.carlauncher.common.util.AppUtil;
import com.wow.carlauncher.common.util.CommonUtil;
import com.wow.carlauncher.ex.manage.time.event.TMEventSecond;
import com.wow.carlauncher.ex.plugin.dudufm.DudufmPlugin;
import com.wow.carlauncher.ex.plugin.music.MusicController;
import com.wow.carlauncher.ex.plugin.music.MusicPlugin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;


/**
 * Created by 10124 on 2017/10/26.
 */

public class DDMusicCarController extends MusicController {
    public static final String PACKAGE_NAME = "com.wow.dudu.music";
    public static final String CLASS_NAME = "com.wow.dudu.music.receiver.MusicCmdReceiver";
    public static final String SERVICE_NAME = "com.wow.dudu.music.service.MainService";

    private static final String ACTION = "com.wow.dudu.music.cmd";

    private static final String CMD = "CMD";
    private static final int CMD_PLAY_OR_PAUSE = 1;
    private static final int CMD_NEXT = 2;
    private static final int CMD_PRE = 3;
    private static final int CMD_REQUEST_LAST = 4;
    private static final int CMD_STOP = 5;

    private static final String RECEIVE_ACTION = "com.wow.dudu.music.notice";

    private static final String TYPE = "TYPE";
    //状态变更
    private static final int STATE_CHANGE = 1;
    private static final String STATE_CHANGE_STATE = "STATE_CHANGE_STATE";

    //歌曲变更
    private static final int SONG_CHANGE = 2;
    private static final String SONG_CHANGE_TITLE = "SONG_CHANGE_TITLE";
    private static final String SONG_CHANGE_SINGER = "SONG_CHANGE_SINGER";
    private static final String SONG_CHANGE_TOTAL_TIME = "SONG_CHANGE_TOTAL_TIME";

    private static final int PROGRESS_CHANGE = 3;
    private static final String CURRENT_PROGRESS = "CURRENT_PROGRESS";

    private static final int COVER_CHANGE = 4;
    private static final String SONG_CHANGE_COVER = "SONG_CHANGE_COVER";

    private static final int LRC_CHANGE = 5;
    private static final String SONG_CHANGE_LRC = "SONG_CHANGE_LRC";

    public void init(Context context, MusicPlugin musicView) {
        super.init(context, musicView);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_ACTION);
        this.context.registerReceiver(mReceiver, intentFilter);
        EventBus.getDefault().register(this);
        sendEvent(CMD_REQUEST_LAST, false);
    }

    @Override
    public String name() {
        return "嘟嘟音乐";
    }

    public void play() {
        sendEvent(CMD_PLAY_OR_PAUSE, true);
    }

    public void pause() {
        if (run) {
            sendEvent(CMD_STOP, true);
        }
    }

    public void next() {
        sendEvent(CMD_NEXT, true);
    }

    public void pre() {
        sendEvent(CMD_PRE, true);
    }

    private void sendEvent(int event, boolean neexCheck) {
        if (neexCheck && !AppUtil.isInstall(context, PACKAGE_NAME)) {
            Toast.makeText(context, "没有安装嘟嘟音乐", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AppUtil.isServiceRunning(context, SERVICE_NAME)) {
            System.out.println("重启服务!!!!!!");
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
            context.startService(serviceIntent);
        }
        Intent intent = new Intent(ACTION);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        intent.putExtra(CMD, event);
        context.sendBroadcast(intent);
        if (event != CMD_STOP) {
            DudufmPlugin.self().stop();
        }
    }

    @Override
    public void destroy() {
        EventBus.getDefault().unregister(this);
        context.unregisterReceiver(mReceiver);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final TMEventSecond event) {
        if (lrcDatas != null) {
            try {
                LrcAnalyze.LrcData lll = null;
                List<LrcAnalyze.LrcData> remove = new ArrayList<>();
                List<LrcAnalyze.LrcData> tempLrc = new ArrayList<>(lrcDatas);
                for (LrcAnalyze.LrcData lrc : tempLrc) {
                    if (lrc.getTimeMs() < nowTime * 1000) {
                        lll = lrc;
                        remove.add(lrc);
                    } else {
                        break;
                    }
                }

                lrcDatas.removeAll(remove);
                if (lll != null) {
                    musicPlugin.refreshLrc(lll.getLrcLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String clazz() {
        return "com.wow.dudu.music";
    }

    private boolean run = false;

    private int nowTime;
    private int totalTime;
    private List<LrcAnalyze.LrcData> lrcDatas;

    private String title, singer;

    private ScheduledFuture coverRefreshTask;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramAnonymousContext, Intent intent) {
            try {
                int type = intent.getIntExtra(TYPE, -1);
                switch (type) {
                    case STATE_CHANGE: {
                        run = intent.getBooleanExtra(STATE_CHANGE_STATE, false);
                        musicPlugin.refreshState(run, true);
                        break;
                    }
                    case SONG_CHANGE: {
                        title = intent.getStringExtra(SONG_CHANGE_TITLE);
                        singer = intent.getStringExtra(SONG_CHANGE_SINGER);
                        musicPlugin.refreshInfo(title, singer, true);
                        totalTime = intent.getIntExtra(SONG_CHANGE_TOTAL_TIME, 0);
                        musicPlugin.refreshProgress(0, totalTime);
                        lrcDatas = null;
                        if (coverRefreshTask != null) {
                            coverRefreshTask.cancel(true);
                            coverRefreshTask = null;
                        }
                        coverRefreshTask = TaskExecutor.self().run(() -> musicPlugin.refreshCover(null), 3000);
                        break;
                    }
                    case PROGRESS_CHANGE: {
                        nowTime = intent.getIntExtra(CURRENT_PROGRESS, 0);
                        if (nowTime <= totalTime && run) {
                            musicPlugin.refreshProgress(nowTime, totalTime);
                        }
                        break;
                    }
                    case COVER_CHANGE: {
                        String title = intent.getStringExtra(SONG_CHANGE_TITLE);
                        String singer = intent.getStringExtra(SONG_CHANGE_SINGER);
                        String cover = intent.getStringExtra(SONG_CHANGE_COVER);
                        if (CommonUtil.equals(title, DDMusicCarController.this.title) && CommonUtil.equals(singer, DDMusicCarController.this.singer) && CommonUtil.isNotNull(cover)) {
                            musicPlugin.refreshCover(null, cover);
                            if (coverRefreshTask != null) {
                                coverRefreshTask.cancel(true);
                                coverRefreshTask = null;
                            }
                        }
                        break;
                    }
                    case LRC_CHANGE: {
                        String title = intent.getStringExtra(SONG_CHANGE_TITLE);
                        String singer = intent.getStringExtra(SONG_CHANGE_SINGER);
                        String lrc = intent.getStringExtra(SONG_CHANGE_LRC);
                        if (CommonUtil.equals(title, DDMusicCarController.this.title) && CommonUtil.equals(singer, DDMusicCarController.this.singer) && CommonUtil.isNotNull(lrc)) {
                            LrcAnalyze lrcAnalyze = new LrcAnalyze(lrc);
                            lrcDatas = lrcAnalyze.lrcList();
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {

            }
        }
    };
}
