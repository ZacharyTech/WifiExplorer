package com.enterprayz.urec.wifiexplorerlib.core;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import com.enterprayz.urec.wifiexplorerlib.enum_state.WIFI_APN_STATE;
import com.enterprayz.urec.wifiexplorerlib.enum_state.WIFI_MODULE_MODE;
import com.enterprayz.urec.wifiexplorerlib.enum_state.WIFI_MODULE_STATE;
import com.enterprayz.urec.wifiexplorerlib.enum_state.WIFI_NET_STATE;
import com.enterprayz.urec.wifiexplorerlib.interfaces.OnAPNCheckListener;
import com.enterprayz.urec.wifiexplorerlib.interfaces.OnAPNUsersChangeStateListener;
import com.enterprayz.urec.wifiexplorerlib.interfaces.OnFinishAPNScanUsersListener;
import com.enterprayz.urec.wifiexplorerlib.interfaces.OnNETCheckListener;
import com.enterprayz.urec.wifiexplorerlib.interfaces.OnWifiModuleCheckListener;
import com.enterprayz.urec.wifiexplorerlib.items.ClientScanResultItem;
import com.enterprayz.urec.wifiexplorerlib.items.WifiScanResultsItem;
import com.enterprayz.urec.wifiexplorerlib.utils.WifiOptions.WifiKeyOptions;
import com.enterprayz.urec.wifiexplorerlib.utils.WifiOptionsBuilder;

import java.util.ArrayList;

/**
 * Wifi Explorer
 *
 * @author Uriy Prayzner (urecki22@gmail.com)
 */
public class WifiClientModel {
    private static ClientCore core;
    private Context context;
    private static WifiClientModel mInstance;


    //Listeners


    private static OnWifiModuleCheckListener wifiModuleCheckListener;
    private boolean isAutoScanEnable;

    /**
     * Set listener.
     *
     * @param listener {@link OnWifiModuleCheckListener}
     */
    public void setWifiModuleCheckListener(OnWifiModuleCheckListener listener) {
        this.wifiModuleCheckListener = listener;
    }

    private static OnAPNCheckListener apnCheckListener;

    /**
     * Set listener.
     *
     * @param listener {@link OnAPNCheckListener}
     */
    public void setOnApnCheckListener(OnAPNCheckListener listener) {
        WifiClientModel.apnCheckListener = listener;
    }

    private static OnFinishAPNScanUsersListener finishAPNScanUsersListener;

    /**
     * Set listener.
     *
     * @param listener {@link OnFinishAPNScanUsersListener}
     */
    public void setOnAPNScanUsersListener(OnFinishAPNScanUsersListener listener) {
        WifiClientModel.finishAPNScanUsersListener = listener;
    }

    private static OnAPNUsersChangeStateListener userChangeStateListener;

    /**
     * Set listener.
     *
     * @param listener {@link OnAPNUsersChangeStateListener}
     */
    public void setAPNUserChangeStateListener(OnAPNUsersChangeStateListener listener) {
        WifiClientModel.userChangeStateListener = listener;
    }

    private static OnNETCheckListener netCheckListener;

    /**
     * Set listener.
     *
     * @param listener {@link OnNETCheckListener}
     */
    public void setOnNetCheckListener(OnNETCheckListener listener) {
        WifiClientModel.netCheckListener = listener;
    }

    //Body
    private WifiClientModel(Context context) {
        this.context = context;
        this.core = new ClientCore(this.context);
    }

    /**
     * Get WifiManager model
     *
     * @return {@link WifiClientModel} singleton
     */
    public static WifiClientModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WifiClientModel(context);
        }
        return mInstance;
    }

    /**
     * Set Wifi module enable/disabled
     */
    public void setWifiModuleEnable(boolean enable) {
        core.setWifiModuleEnabled(enable);
    }

    /**
     * Get Wifi module state
     * @return {@link boolean} is tunr on/off
     */
    public boolean isWifiModuleEnabled() {
        return core.isWifiModuleEnabled();
    }
    /**
     * Scan wifi for new networks. The results is returning in {@link OnNETCheckListener} .onNetworkScanResult.
     */
    public void scanNetwork() {
        core.scanWifiNetwork();
    }

    /**
     * Set auto scan monitor for new networks. The results is returning in {@link OnNETCheckListener} .onNetworkScanResult. Remember, android system scan wifi network  without you.
     * @param dellay through in which time  scan network.
     */
    public void autoScanNetwork(final int dellay) {
        isAutoScanEnable = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isAutoScanEnable && netCheckListener != null) {
                    scanNetwork();
                    try {
                        Thread.sleep(dellay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Stop scan monitor
     * */
    public void stopAutoScanNetwork() {
        if (isAutoScanEnable) {
            isAutoScanEnable = false;
        }
    }

    /**
     * Get Wifi network scanner state
     *
     * @return <code>boolean</code> - scanner state
     */
    public boolean getAutoScanCheck() {
        return isAutoScanEnable;
    }

    /**
     * Connect to Wifi network.
     *
     * @param options {@link WifiKeyOptions} configured wifi options.
     */
    public void connectToNetwork(WifiKeyOptions options) {
        WifiConfiguration configuration = new WifiOptionsBuilder().getConfig(options);
        core.connectToNetwork(configuration);
    }

    /**
     * Disconnect from connected Wifi network.
     */
    public void dissconnectFromNetwork() {
        core.disconnectFromNetwork();
    }

    /**
     * Get SSID of connected Wifi network
     *
     * @return <code>String</code> network SSID
     */
    public String getSSIDofConnectedWifi() {
        return core.getSSIDofCurrentWifi();
    }

    /**
     * Create APN with configured options.
     *
     * @param options {@link WifiKeyOptions} configured wifi options.
     */
    public void createAp(WifiKeyOptions options) {
        WifiConfiguration configuration = new WifiOptionsBuilder().getConfig(options);
        core.createAp(configuration);
    }

    /**
     * Close APN.
     */
    public void closeAP() {
        core.closeAP();
    }

    /**
     * Get last saved {@link WifiConfiguration} of APN.
     *
     * @return {@link WifiConfiguration} saved wifi network configuration.
     */
    public WifiConfiguration getLastApnConfiguration() {
        return core.getLastAPNConfiguration();
    }

    /**
     * Send request for getting list of APN users. Result returns in {@link OnFinishAPNScanUsersListener}.
     */
    public void getAPNUsers() {
        core.getClientsList();
    }

    /**
     * Get wifi module mode.
     *
     * @return {@link WIFI_MODULE_MODE} wifi module mode.
     */
    public WIFI_MODULE_MODE getWifiModuleMode() {
        WIFI_MODULE_MODE mode = WIFI_MODULE_MODE.OFF_MODE;
        if (getModuleState().equals(WIFI_MODULE_STATE.WIFI_MODULE_STATE_ENABLED) && getAPNState().equals(WIFI_APN_STATE.WIFI_AP_STATE_DISABLED)) {
            mode = WIFI_MODULE_MODE.NET_MODE;
        } else if (getModuleState().equals(WIFI_MODULE_STATE.WIFI_MODULE_STATE_DISABLED) && getAPNState().equals(WIFI_APN_STATE.WIFI_AP_STATE_ENABLED)) {
            mode = WIFI_MODULE_MODE.APN_MODE;
        }
        return mode;
    }

    /**
     * Get APN state.
     *
     * @return {@link WIFI_APN_STATE} APN module state.
     */
    public WIFI_APN_STATE getAPNState() {
        return core.getWifiApState();
    }

    /**
     * Get wifi module state.
     *
     * @return {@link WIFI_MODULE_STATE} wifi module state.
     */
    public WIFI_MODULE_STATE getModuleState() {
        return core.getWifiModuleState();
    }

    /**
     * Get NET state.
     *
     * @return {@link WIFI_NET_STATE} NET state.
     */
    public WIFI_NET_STATE getNETState() {
        return core.getWifiNetState();
    }

    /**
     * Get all networks.
     *
     *
     * @return {@link ArrayList<WifiScanResultsItem>} wifi available networks.
     */
    public ArrayList<WifiScanResultsItem> getAllNetwork() {
        return core.getScanResults();
    }

    //Anonymous class. Contains actions of singleton.
    static class Actions {
        private static String TAG = Actions.class.getName();

        public static void setOnWifiModuleEnableCheck(WIFI_MODULE_STATE state) {
            if (wifiModuleCheckListener != null) {

                wifiModuleCheckListener.onWifiModuleChangeCheck(state);
            }
        }

        public static void setOnAPNSheckListenerState(int state) {
            if (apnCheckListener != null) {
                try {
                    apnCheckListener.onAPNChangeCheck(WIFI_APN_STATE.getStateByIndex(state));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        public static void setOnFinishScanAPNUsers(ArrayList<ClientScanResultItem> list, boolean isScanMonitor) {
            if (finishAPNScanUsersListener != null) {
                if (!isScanMonitor) {
                    finishAPNScanUsersListener.onGetAPNUsers(list);
                }
            }
        }

        public static void setOnAPNUserChangeState(ClientScanResultItem userItem, int flag) {
            if (userChangeStateListener != null) {
                userChangeStateListener.onAPNUsersChangeState(userItem, flag);
            }
        }

        public static void setOnNETChangeState(WIFI_NET_STATE state) {
            if (netCheckListener != null) {
                netCheckListener.onNetCheckChange(state);
            }
        }

        public static void setOnNetworkScanCheckChange() {
            if (netCheckListener != null) {
                netCheckListener.onNetworkScanResult(core.getScanResults());
            }
        }
    }
}

