package alc.journal.utils;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class SnackbarUtils {
    private View mView;
    private int mDuration;
    private String mText;
    private int mBackgroundColor;
    private int mTextColor;
    private int mButtonColor;

    public SnackbarUtils(View aView, String aText, int aBgColor, int aTextColor, int aButtonColor, int aDuration){
        this.mView = aView;
        this.mDuration = aDuration;
        this.mText = aText;
        this.mBackgroundColor = aBgColor;
        this.mTextColor = aTextColor;
        this.mButtonColor = aButtonColor;
    }

    public Snackbar snackbar(){
        Snackbar snackie = Snackbar.make(mView, mText, mDuration);
        View snackView = snackie.getView();
        TextView snackViewText = snackView.findViewById(android.support.design.R.id.snackbar_text);
        Button snackViewButton = snackView.findViewById(android.support.design.R.id.snackbar_action);
        snackView.setBackgroundColor(mBackgroundColor);
        snackViewText.setTextColor(mTextColor);
        snackViewButton.setTextColor(mButtonColor);
        return snackie;
    }

}

