package com.mparticle.push;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;

import com.mparticle.MParticlePushUtility;

/**
 * Created by sdozor on 10/17/14.
 */
public class CloudDialog extends DialogFragment {

    private static final String DARK_THEME = "mp.dark";
    private static final String LIGHT_THEME = "mp.light";

    public static String TAG = "mp_dialog";

    public static CloudDialog newInstance(MPCloudMessage message) {
        CloudDialog frag = new CloudDialog();
        Bundle args = new Bundle();
        args.putParcelable(MParticlePushUtility.CLOUD_MESSAGE_EXTRA, message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MPCloudMessage message = getArguments().getParcelable(MParticlePushUtility.CLOUD_MESSAGE_EXTRA);

        int iconId = android.R.drawable.ic_dialog_alert;
        try {
            iconId = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0).icon;
        } catch (PackageManager.NameNotFoundException e) {
            // use the ic_dialog_alert icon if the app's can not be found
        }

        String theme = message.getInAppTheme();
        int themeId = 0;
        if (theme != null && !theme.equals(DARK_THEME) && !theme.equals(LIGHT_THEME)){
            try {
                themeId = getResources().getIdentifier(theme, "style", getActivity().getPackageName());
            }catch (Exception e){

            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (DARK_THEME.equals(theme)) {
                themeId = AlertDialog.THEME_HOLO_DARK;
            } else {
                themeId = AlertDialog.THEME_HOLO_LIGHT;
            }
        }

        AlertDialog.Builder dialog;

        if (themeId > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                dialog = new AlertDialog.Builder(getActivity(), themeId);
            } else {
                ContextThemeWrapper ctw = new ContextThemeWrapper( getActivity(), themeId );
                dialog = new AlertDialog.Builder(ctw);
            }
        }else{
            dialog = new AlertDialog.Builder(getActivity());
        }

        dialog.setIcon(iconId);
        dialog.setTitle(message.getContentTitle(getActivity()));

        String primary = message.getPrimaryText(getActivity());
        String bigText = message.getBigText();
        if (bigText != null){
            dialog.setMessage(bigText);
        }else{
            dialog.setMessage(primary);
        }

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() instanceof DialogInterface.OnClickListener){
                    ((DialogInterface.OnClickListener)getActivity()).onClick(dialog, which);
                }
                dialog.dismiss();
            }
        });

        return dialog.create();
    }
}
