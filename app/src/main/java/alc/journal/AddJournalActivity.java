package alc.journal;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import alc.journal.utils.GlideApp;
import alc.journal.utils.Util;
import alc.journal.model.Journal;

public class AddJournalActivity extends AppCompatActivity {

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabase;
    private ImageView mImageView ;
    private EditText mEditTextTitle,mEditTextText;
    private String mImageUrl;
    private String mUserUid;
    private boolean mEditJournal;
    private String mJournalEntryKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_journal);
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mUserUid = bundle.getString(Util.USER_UID);
                mEditJournal = bundle.getBoolean(Util.EDIT_JOURNAL);
                if (mEditJournal)
                    mJournalEntryKey = bundle.getString(Util.JOURNAL_ENTRY_KEY);

            }
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            FirebaseStorage mFirebeStorage = FirebaseStorage.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference(Util.DATA_BASE_NAME);
            mStorageReference = mFirebeStorage.getReference();
            mImageView = findViewById(R.id.add_image);
            mEditTextTitle = findViewById(R.id.journal_title);
            mEditTextText = findViewById(R.id.journal_text);
            ImageView addBtn = findViewById(R.id.add_btn);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseImage();
                }
            });
            this.setTitle(Util.getTimNow());
            if (mEditJournal)
                displayJournal();
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture_label)), PICK_IMAGE_REQUEST);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_journal, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.mn_add:
                uploadImage();
                return true;
           // case R.id.mn_cancel:
           //     onBackPressed();
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                filePath = data.getData();
                Glide.with(AddJournalActivity.this).load(filePath).into(mImageView);
            }
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }

    private void uploadImage() {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            mImageUrl  = "images/"+ UUID.randomUUID().toString();
            StorageReference ref = mStorageReference.child(mImageUrl);
            if(filePath == null) {
                progressDialog.dismiss();
                Util.showMessageBoxSimple(this,getResources().getString(R.string.picture_label),
                                                       getResources().getString(R.string.picture_error));
            }else if(TextUtils.isEmpty(mEditTextTitle.getText().toString())){
                progressDialog.dismiss();
                mEditTextTitle.setFocusable(true);
                mEditTextTitle.setError(getResources().getString(R.string.title_error));
            }else if (TextUtils.isEmpty(mEditTextText.getText().toString())){
                progressDialog.dismiss();
                mEditTextText.setFocusable(true);
                mEditTextText.setError(getResources().getString(R.string.text_error));
            }else{
              Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    String realPath = Util.getRealPathFromURI_API19(AddJournalActivity.this,filePath);
                    bmp = rotateImageIfRequired(bmp,realPath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                    byte[] fileInBytes = baos.toByteArray();
                    //ref.putFile(filePath)
                   ref.putBytes(fileInBytes)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    addToDatabase();
                                    progressDialog.dismiss();
                                    Toast.makeText(AddJournalActivity.this, getResources().getString(R.string.save_succ), Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddJournalActivity.this, getResources().getString(R.string.save_fail)+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage(getResources().getString(R.string.progress)+(int)progress+"%");
                                }
                            });
                } catch (Exception ex) {
                    Util.showMessageBoxSimple(this,Util.APP_NAME,ex.getMessage());
                }
            }
    }

    private void addToDatabase() {
        try {
            if (!mEditJournal) {
                Journal myJournal = new Journal(mUserUid, mEditTextTitle.getText().toString(),
                        mEditTextText.getText().toString(),
                        mImageUrl,
                        Util.getTimNow(),
                        Util.getDateInt(AddJournalActivity.this));
                mDatabase.push().setValue(myJournal);
            } else {
                mDatabase.child(mJournalEntryKey).child("mText").setValue(mEditTextText.getText().toString());
                mDatabase.child(mJournalEntryKey).child("mTitle").setValue(mEditTextTitle.getText().toString());
                mDatabase.child(mJournalEntryKey).child("mImageUrl").setValue(mImageUrl);
            }
            onBackPressed();

        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }
    private void displayJournal() {
        try {
            mDatabase = FirebaseDatabase.getInstance().getReference(Util.DATA_BASE_NAME);
            Query connectedUser = mDatabase.child(mJournalEntryKey);
            connectedUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Journal mJournaSnapshot = dataSnapshot.getValue(Journal.class);
                    if (mJournaSnapshot != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mJournaSnapshot.getmImageUrl());
                        GlideApp.with(AddJournalActivity.this)
                                .load(storageReference)
                                .into(mImageView);
                        mEditTextText.setText(mJournaSnapshot.getmText());
                        mEditTextTitle.setText(mJournaSnapshot.getmTitle());
                        setTitle(mJournaSnapshot.getmDate());

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }


    private Bitmap rotateImageIfRequired(Bitmap img, String uriImage) throws  IOException{
        ExifInterface ei = new ExifInterface(uriImage);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
    }

}
