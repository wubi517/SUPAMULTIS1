package com.it_tech613.tvmulti.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;

public class ParentContrlDlg extends Dialog {
    private String old_pass;
    private String new_pass;
    private String confirm_pass;
    private int code;
    public ParentContrlDlg(@NonNull Context context, final ParentContrlDlg.DialogUpdateListener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_parent);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final EditText txt_old = (EditText)findViewById(R.id.txt_old_pass);
        final EditText txt_new = (EditText)findViewById(R.id.txt_new_pass);
        final EditText txt_confirm = (EditText)findViewById(R.id.txt_pass_confirm);
        old_pass = (String) MyApp.instance.getPreference().get(Constants.getPIN_CODE());
        txt_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                new_pass = txt_new.getText().toString();
                if(!txt_confirm.getText().toString().equalsIgnoreCase(new_pass) && txt_confirm.getText().toString().length()==4){
                    Toast.makeText(getContext(),"Please confirem again", Toast.LENGTH_SHORT).show();
                    txt_confirm.setText("");
                }
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnUpdateSkipClick(ParentContrlDlg.this,0);
            }
        });
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!txt_old.getText().toString().equalsIgnoreCase(old_pass)){
                    Toast.makeText(getContext(),"Please confirm old password again", Toast.LENGTH_SHORT).show();
                    txt_old.setText("");
                }else {
                    if(new_pass.length()==4){
                        MyApp.instance.getPreference().remove(Constants.getPIN_CODE());
                        MyApp.instance.getPreference().put(Constants.getPIN_CODE(),new_pass);
                    }
                    listener.OnUpdateNowClick(ParentContrlDlg.this,1);
                }
            }
        });
    }

    public interface DialogUpdateListener {
        public void OnUpdateNowClick(Dialog dialog, int code);
        public void OnUpdateSkipClick(Dialog dialog, int code);
    }
}
