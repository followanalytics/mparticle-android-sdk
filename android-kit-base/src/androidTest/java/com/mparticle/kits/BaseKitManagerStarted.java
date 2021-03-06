package com.mparticle.kits;

import android.content.Context;

import com.mparticle.testutils.BaseCleanInstallEachTest;
import com.mparticle.MParticle;
import com.mparticle.MParticleOptions;
import com.mparticle.internal.AccessUtils;
import com.mparticle.internal.AppStateManager;
import com.mparticle.internal.BackgroundTaskHandler;
import com.mparticle.internal.ConfigManager;
import com.mparticle.internal.ReportingManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class BaseKitManagerStarted extends BaseCleanInstallEachTest {
    private static Map<Integer, String> mCustomTestKits;
    protected Long mStartingMpid;
    protected KitManagerImpl mKitManager;

    @Before
    public void before() throws Exception {
        mStartingMpid = new Random().nextLong();
        setupConfigMessageForKits(registerCustomKits());
        new ConfigManager(mContext, null, null, null).setMpid(mStartingMpid);
        mServer.setupHappyIdentify(mStartingMpid);
        MParticle.setInstance(null);
        MParticle.start(MParticleOptions.builder(mContext)
                .credentials("key", "value")
                .build());
        mKitManager = new CustomKitManagerImpl(mContext, com.mparticle.AccessUtils.getMessageManager(), MParticle.getInstance().getConfigManager(), MParticle.getInstance().getAppStateManager(), AccessUtils.getUploadHandler());
        mKitManager.setKitFactory(new CustomKitFactory());
        AccessUtils.setKitManager(mKitManager);
    }

    //implementing this method will both register your custom kit, and start it via modifying the
    //config response to contains an "eks" message with the kit's id
    protected abstract Map<String, JSONObject> registerCustomKits();

    private void setupConfigMessageForKits(Map<String, JSONObject> kitIds) {
        JSONArray eks = new JSONArray();
        int i = -1;
        mCustomTestKits = new HashMap<>();
        for (Map.Entry<String, JSONObject> kitConfig: kitIds.entrySet()) {
            try {
                mCustomTestKits.put(i, kitConfig.getKey());
                JSONObject configJson = new JSONObject();
                if (kitConfig.getValue() != null) {
                    configJson = kitConfig.getValue();
                }
                configJson.put("id", i);
                eks.put(configJson);
            } catch (JSONException e) {
                throw new RuntimeException(String.format("Kit class %s unable to be set", kitConfig.getKey()));
            }
            i--;
        }
        try {
            JSONObject configObject = new JSONObject().put("eks", eks);
            mServer.setupConfigResponse(configObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException("Error sending custom eks to config");
        }
    }

    //this is a non-anonymous class only for the purpose of debugging
    class CustomKitManagerImpl extends KitManagerImpl {

        private Runnable kitsStartedListener;
        private boolean started;
        public void setOnKitsStartedListener(Runnable runnable) {
            if (started) {
                if (kitsStartedListener != null) {
                    kitsStartedListener.run();
                }
            } else {
                kitsStartedListener = runnable;
            }
        }

        public CustomKitManagerImpl(Context context, ReportingManager reportingManager, ConfigManager configManager, AppStateManager appStateManager, BackgroundTaskHandler backgroundTaskHandler) {
            super(context, reportingManager, configManager, appStateManager, backgroundTaskHandler);
        }

        @Override
        public void updateKits(JSONArray kitConfigs) {
            super.updateKits(kitConfigs);
            started = true;
            if (kitsStartedListener != null) {
                kitsStartedListener.run();
                kitsStartedListener = null;
            }

        }
    }

    class CustomKitFactory extends KitIntegrationFactory {

        @Override
        protected Map<Integer, String> getKnownIntegrations() {
            Map<Integer, String> kitIntegration = super.getKnownIntegrations();
            if (mCustomTestKits != null) {
                for (Integer key: mCustomTestKits.keySet()) {
                    if (kitIntegration.containsKey(key)) {
                        throw new RuntimeException(String.format("Key value %d is already an existing kit, use a unique kitId for tests, please", key));
                    }
                }
                kitIntegration.putAll(mCustomTestKits);
            }
            return kitIntegration;
        }
    }
}
