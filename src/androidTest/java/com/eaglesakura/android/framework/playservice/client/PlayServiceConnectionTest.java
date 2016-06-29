package com.eaglesakura.android.framework.playservice.client;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PlayServiceConnectionTest extends DeviceTestCase {

    @Test
    public void PlayServiceに接続が行える() throws Throwable {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API);
        PlayServiceConnection connection = PlayServiceConnection.newInstance(builder, () -> false);

        assertNotNull(connection);
        assertTrue(connection.isConnected());   // 権限の要らない接続なので接続できるべき
    }
}
