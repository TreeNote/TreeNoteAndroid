package de.treenote.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;

public class TreeNoteResultReceiver extends ResultReceiver {

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public static Parcelable.Creator CREATOR = ResultReceiver.CREATOR;

    private Receiver mReceiver;

    public TreeNoteResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}