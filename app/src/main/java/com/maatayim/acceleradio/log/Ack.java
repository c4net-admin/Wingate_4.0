package com.maatayim.acceleradio.log;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.maatayim.acceleradio.MainActivity;
import com.maatayim.acceleradio.utils.FormatException;

import static com.maatayim.acceleradio.Parameters.ACK;
import static com.maatayim.acceleradio.Parameters.DELIMITER_RX;
import static com.maatayim.acceleradio.Parameters.SUB_DELIMITER;

class Ack extends LogEntry {
    String num;
    public Ack(String str) throws FormatException {
        super(str);
        parseStr();
    }

    private void parseStr() throws FormatException {
        if (!TextUtils.isEmpty(entry)){
            String[] strBuffer = entry.split(SUB_DELIMITER);

            if (strBuffer != null && strBuffer.length ==2 && strBuffer[0].equals(ACK)){
                num = strBuffer[1].replace(DELIMITER_RX,"");
            }
        }
    }

    @Override
    public void handle(Activity mainActivity, ImageView button) {
        //todo handel received ack
       // ((MainActivity)mainActivity).
    }
}
