package alc.journal.model;

public class Journal {

    private String mUserUid ;
    private String mTitle ;
    private String mText;
    private String mImageUrl;
    private String mDate;
    private String mDateInt;

    public Journal(){

    }
    public Journal(String userUid, String title, String text, String imageUrl, String date, String dateInt){
        this.mUserUid = userUid;
        this.mTitle = title;
        this.mText = text;
        this.mImageUrl = imageUrl;
        this.mDate = date;
        this.mDateInt = dateInt;
    }

    public String getmUserUid() {
        return mUserUid;
    }

    public void setmUserUid(String mUserUid) {
        this.mUserUid = mUserUid;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmDateInt() {
        return mDateInt;
    }

    public void setmDateInt(String mDateInt) {
        this.mDateInt = mDateInt;
    }
}
