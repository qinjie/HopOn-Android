package com.example.sonata.hop_on.BeaconService;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BeaconScanningService extends Service implements BeaconConsumer{

    private static final String UUID = "23A01AF0-232A-4518-9C0E-323FB773F5EF";
    private static final Region[] BEACONS = new Region[]{
            new Region("Scanned Region", Identifier.parse(UUID), null, null)
    };
    private BeaconManager beaconManager;

    private ArrayList<String> arrayList;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        arrayList = intent.getStringArrayListExtra("arrayList");
        handler.sendEmptyMessageDelayed(1, 5 * 1000);
        return START_STICKY;
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(5));
        beaconManager.setBackgroundBetweenScanPeriod(1000);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                try{
                    if (beacons.size() >= arrayList.size() / 3) {
                        ArrayList<Beacon> array = new ArrayList<>();
                        array.addAll(beacons);
                        checkReturnOrUnlockAble(array);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        try {
            for (Region region : BEACONS) {
                beaconManager.startRangingBeaconsInRegion(region);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkReturnOrUnlockAble(ArrayList<Beacon> array){
        int cnt = 0;
        for(int i = 0; i < arrayList.size(); i++){
            boolean found = false;
            for(int k = 0; k < array.size(); k++){
                if(array.get(k).getIdentifiers().get(i % 3).toString().toUpperCase().equals(arrayList.get(i))){
                    cnt++;
                    found = true;
                    break;
                }
            }
            if(!found)
                break;
        }
        if(cnt == arrayList.size()){
            Intent intent = new Intent("OK_to_Return_or_Unlock");
            intent.putExtra("type", cnt == 6 ? "Return" : "Unlock");
            sendBroadcast(intent);
            stopSelf();
        }
    }
}
