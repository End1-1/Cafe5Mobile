package com.example.cafe5mobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class Dlg extends Dialog implements View.OnClickListener{

    public interface DlgButtonClick {
        void click();
    }

    public Dlg(Context context) {
        super(context);
    }

    private DlgButtonClick mClickYes;
    private DlgButtonClick mClickNo;
    private DlgButtonClick mClickOk;

    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.custom_dialog);
        findViewById(R.id.btn_yes).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_no).setOnClickListener(this);
    }

    public static Dlg createDialog(Context context, String message) {
        Dlg d = new Dlg(context);
        d.show();
        ((TextView) d.findViewById(R.id.msg)).setText(message);
        return d;
    }

    public Dlg setYes(DlgButtonClick yes) {
        mClickYes = yes;
        findViewById(R.id.btn_yes).setVisibility(View.VISIBLE);
        return this;
    }

    public Dlg setNo(DlgButtonClick no) {
        mClickNo = no;
        findViewById(R.id.btn_no).setVisibility(View.VISIBLE);
        return this;
    }

    public Dlg setOk(DlgButtonClick ok) {
        mClickOk = ok;
        findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (mClickYes != null) {
                    mClickYes.click();
                }
                break;
            case R.id.btn_no:
                if (mClickNo != null) {
                    mClickNo.click();
                }
                break;
            case R.id.btn_ok:
                if (mClickOk != null) {
                    mClickOk.click();
                }
                break;
            default:
                break;
        }
        dismiss();
    }
}
