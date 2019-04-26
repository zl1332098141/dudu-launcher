package com.wow.carlauncher.view.activity.set.view;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import com.wow.carlauncher.R;
import com.wow.carlauncher.common.CommonData;
import com.wow.carlauncher.common.util.CommonUtil;
import com.wow.carlauncher.common.util.SharedPreUtil;
import com.wow.carlauncher.common.util.ThreadObj;
import com.wow.carlauncher.common.view.SetView;
import com.wow.carlauncher.ex.manage.ble.BleManage;
import com.wow.carlauncher.ex.manage.toast.ToastManage;
import com.wow.carlauncher.ex.plugin.fk.FangkongPlugin;
import com.wow.carlauncher.ex.plugin.fk.FangkongProtocolEnum;
import com.wow.carlauncher.view.base.BaseView;
import com.wow.carlauncher.view.dialog.ListDialog;

import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import static com.wow.carlauncher.common.CommonData.SDATA_FANGKONG_CONTROLLER;

/**
 * Created by 10124 on 2018/4/22.
 */

public class SFkView extends BaseView {
    public static final FangkongProtocolEnum[] ALL_FANGKONG_CONTROLLER = {FangkongProtocolEnum.YLFK};

    public SFkView(@NonNull Context context) {
        super(context);
    }

    public SFkView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getContent() {
        return R.layout.content_set_fk;
    }

    @ViewInject(R.id.sv_fangkong_select)
    private SetView sv_fangkong_select;

    @ViewInject(R.id.sv_fangkong_impl_select)
    private SetView sv_fangkong_impl_select;

    @ViewInject(R.id.sv_fangkong_remove)
    private SetView sv_fangkong_remove;

    @Override
    protected void initView() {

        FangkongProtocolEnum fkp1 = FangkongProtocolEnum.getById(SharedPreUtil.getInteger(SDATA_FANGKONG_CONTROLLER, FangkongProtocolEnum.YLFK.getId()));
        sv_fangkong_impl_select.setSummary("方控使用的协议：" + fkp1.getName());
        sv_fangkong_impl_select.setOnClickListener(view -> {
            FangkongProtocolEnum p = FangkongProtocolEnum.getById(SharedPreUtil.getInteger(SDATA_FANGKONG_CONTROLLER, FangkongProtocolEnum.YLFK.getId()));
            final FangkongProtocolEnum[] show = ALL_FANGKONG_CONTROLLER;
            String[] items = new String[show.length];
            int select = 0;
            for (int i = 0; i < show.length; i++) {
                items[i] = show[i].getName();
                if (show[i].equals(p)) {
                    select = i;
                }
            }
            final ThreadObj<Integer> obj = new ThreadObj<>(select);
            AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("请选择协议").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreUtil.saveInteger(SDATA_FANGKONG_CONTROLLER, show[obj.getObj()].getId());
                    FangkongPlugin.self().disconnect();
                    sv_fangkong_impl_select.setSummary("方控使用的协议：" + show[obj.getObj()].getName());
                }
            }).setSingleChoiceItems(items, select, (dialog12, which) -> obj.setObj(which)).show();
        });


        sv_fangkong_remove.setOnClickListener(view -> {
            SharedPreUtil.saveString(CommonData.SDATA_FANGKONG_ADDRESS, null);
            SharedPreUtil.saveString(CommonData.SDATA_FANGKONG_NAME, null);
            FangkongPlugin.self().disconnect();
            ToastManage.self().show("方控绑定已删除");
            sv_fangkong_select.setSummary("没有绑定蓝牙设备");
        });


        String fkaddress = SharedPreUtil.getString(CommonData.SDATA_FANGKONG_ADDRESS);
        if (CommonUtil.isNotNull(fkaddress)) {
            sv_fangkong_select.setSummary("绑定了设备:" + SharedPreUtil.getString(CommonData.SDATA_FANGKONG_NAME) + "  地址:" + fkaddress);
        } else {
            sv_fangkong_select.setSummary("没有绑定蓝牙设备");
        }
        sv_fangkong_select.setOnClickListener(view -> {
            final ThreadObj<ListDialog> listTemp = new ThreadObj<>();
            final List<BluetoothDevice> devices = new ArrayList<>();
            BleManage.self().searchBle(device -> {
                boolean have = false;
                for (BluetoothDevice d : devices) {
                    if (d.getAddress().equals(device.getAddress())) {
                        have = true;
                        break;
                    }
                }
                if (!have) {
                    devices.add(device.device);
                    String[] items = new String[devices.size()];
                    for (int i = 0; i < items.length; i++) {
                        BluetoothDevice bluetoothDevice = devices.get(i);
                        items[i] = bluetoothDevice.getName() + ":" + bluetoothDevice.getAddress();
                    }
                    listTemp.getObj().getListView().setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items));
                }
            });

            final ListDialog dialog = new ListDialog(getContext());
            dialog.setOnDismissListener(dialogInterface -> BleManage.self().stopSearch());
            dialog.setTitle("请选择一个蓝牙设备");
            dialog.show();
            listTemp.setObj(dialog);

            dialog.getListView().setOnItemClickListener((adapterView, view1, i, l) -> {
                dialog.dismiss();
                BluetoothDevice device = devices.get(i);
                SharedPreUtil.saveString(CommonData.SDATA_FANGKONG_ADDRESS, device.getAddress());
                SharedPreUtil.saveString(CommonData.SDATA_FANGKONG_NAME, device.getName());
                sv_fangkong_select.setSummary("绑定了设备:" + device.getName() + "  地址:" + device.getAddress());
            });
        });
    }
}