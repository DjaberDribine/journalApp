package alc.journal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Arrays;
import alc.journal.utils.GlideApp;
import alc.journal.utils.SnackbarUtils;
import alc.journal.utils.Util;
import alc.journal.adapter.FirebaseListAdapter;
import alc.journal.model.Journal;

public class HomeActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    private FirebaseListAdapter<Journal> mJournalListAdapter;
    private ListView mJournalListView;
    private ProgressBar mProgressBar;
    private String mUsername;
    private String mUserUid;
    private ImageView mUserPhoto;
    private TextView mUserDisplayName;
    private View mNoItemToShow;
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);
            mNoItemToShow = findViewById(R.id.no_items);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            this.setTitle("");


            mUserPhoto = findViewById(R.id.user_photo);
            mUserDisplayName = findViewById(R.id.user_display_name);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (Util.isPermissionGranted(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                Util.isPermissionGranted(HomeActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            goToNewJournalActivity();
                        } else {
                            requestPermissions(Util.GALLERY_PERMISSION, Util.STORAGE_PERMISSION_CODE);
                        }
                    } else {
                        goToNewJournalActivity();
                    }
                }
            });

            mUsername = ANONYMOUS;

            // Initialize firebase
            mFirebaseAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference(Util.DATA_BASE_NAME);

            // Initialize references to views
            mProgressBar = findViewById(R.id.progressBar);
            mJournalListView = findViewById(R.id.journal_listView);


            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        //createUserDatabase(user);
                        mUserUid = user.getUid();
                        mUserDisplayName.setText(user.getDisplayName());
                        Glide.with(HomeActivity.this).load(user.getPhotoUrl()).into(mUserPhoto);
                        displayJournal();
                        onSignedInInitialize(user.getDisplayName());
                    } else {
                        onSignedOutCleanup();
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(
                                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                                        new AuthUI.IdpConfig.GoogleBuilder().build()
                                                        //Arrays.asList( new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()

                                                ))
                                        .build(),
                                Util.RC_SIGN_IN);
                    }
                }
            };


            mJournalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    DatabaseReference itemRef = mJournalListAdapter.getRef(position);
                    Intent intentDetail = new Intent(HomeActivity.this, JournalDetailActivity.class);
                    intentDetail.putExtra(Util.JOURNAL_ENTRY_KEY, itemRef.getKey());
                    startActivity(intentDetail);
                }
            });
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }

    private void goToNewJournalActivity(){
        Intent intent = new Intent(HomeActivity.this,AddJournalActivity.class);
        intent.putExtra(Util.USER_UID,mUserUid);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case  R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            //case R.id.show_menu:
               // item.setIcon(getDrawable(Util.getBooleanPreference(HomeActivity.this,Util.DISPLAY_ITEM_BAR_PREF)?R.drawable.baseline_view_list_24: R.drawable.baseline_view_agenda_24));
                //Util.setBooleanPreference(HomeActivity.this,Util.DISPLAY_ITEM_BAR_PREF,!Util.getBooleanPreference(HomeActivity.this,Util.DISPLAY_ITEM_BAR_PREF));


              //  return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayJournal() {
        try {
           // int view  =  Util.getBooleanPreference(HomeActivity.this,Util.DISPLAY_ITEM_BAR_PREF)?R.layout.item_journal_v:R.layout.item_journal;
            Query query = mDatabase.orderByChild("mUserUid").equalTo(mUserUid);
            FirebaseListOptions<Journal> options =
                    new FirebaseListOptions.Builder<Journal>()
                            .setQuery(query, Journal.class)
                            .setLayout(R.layout.item_journal)
                            .build();
            mJournalListAdapter = new FirebaseListAdapter<Journal>(options) {

                @Override
                protected void populateView(View v, Journal model, int position) {
                    ImageView journalImage = v.findViewById(R.id.journal_image);
                    TextView journalTitle = v.findViewById(R.id.journal_title);
                    TextView journalDate = v.findViewById(R.id.journal_date);
                    TextView journalText = v.findViewById(R.id.journal_text);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(model.getmImageUrl());
                    GlideApp.with(journalImage.getContext())
                            .load(storageReference)
                            .into(journalImage);
                    journalText.setText(model.getmText());
                    journalTitle.setText(model.getmTitle());
                    journalDate.setText(model.getmDate());

                    if(mNoItemToShow.getVisibility() == View.VISIBLE)
                        mNoItemToShow.setVisibility(View.INVISIBLE);

                }
            };
            mJournalListView.setAdapter(mJournalListAdapter);

            DatabaseReference list = mDatabase.orderByChild("mUserUid").equalTo(mUserUid).getRef();
            list.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Util.RC_SIGN_IN){
            if(resultCode == RESULT_OK){

            }else if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Util.isInternetAvailable(this))
            showNoConnectionDialog();
        else
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        if(mJournalListAdapter != null)
        mJournalListAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mJournalListAdapter != null)
        mJournalListAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mJournalListAdapter != null)
        mJournalListAdapter.stopListening();
    }




    private void onSignedInInitialize(String username){
        mUsername = username;
        if(mJournalListAdapter != null)
        mJournalListAdapter.startListening();
    }

    private void onSignedOutCleanup(){
        mUsername = ANONYMOUS;
        if(mJournalListAdapter != null)
        mJournalListAdapter.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try{
                if(Util.STORAGE_PERMISSION_CODE == requestCode){
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        goToNewJournalActivity();
                    } else {
                        new SnackbarUtils(findViewById(android.R.id.content),getResources().getString(R.string.grant_permission_msg),
                                getResources().getColor(R.color.red), Color.WHITE, Color.WHITE, 0).snackbar()
                                .setAction(getResources().getString(R.string.yes), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= 23) {
                                            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.STORAGE_PERMISSION_CODE);
                                        }
                                    }
                                }).show();
                    }
                }
        }catch (Exception ex) {
         Util.showMessageBoxSimple(this,Util.APP_NAME,ex.getMessage());
        }

    }

    private void showNoConnectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection");
        builder.setMessage("No connections are available!!\nMake sure WI-FI or cellular-data isturned on, then try again.")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog alertDialog =  builder.create();
        alertDialog.show();
    }
}
