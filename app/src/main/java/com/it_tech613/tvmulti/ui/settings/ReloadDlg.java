package com.it_tech613.tvmulti.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import com.it_tech613.tvmulti.R;

/**
 * Created by RST on 3/23/2018.
 */

public class ReloadDlg extends Dialog {
    public ReloadDlg(@NonNull Context context, final ReloadDlg.DialogUpdateListener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_reload);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnUpdateSkipClick(ReloadDlg.this);
            }
        });
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listener.OnUpdateNowClick(ReloadDlg.this);
            }
        });
    }

    public interface DialogUpdateListener {
        public void OnUpdateNowClick(Dialog dialog);
        public void OnUpdateSkipClick(Dialog dialog);
    }
}
