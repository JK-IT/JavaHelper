package jkit.looptime;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by jkgra on 11/16/2017.
 */

public class RingtoneService extends Service {
    private Ringtone rt;
    public RingtoneService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        rt = RingtoneManager.getRingtone(this, ringUri);
        rt.play();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        rt.stop();
    }

    public static Intent newIntent(Context context)
    {
        return new Intent(context, RingtoneService.class);
    }
}
