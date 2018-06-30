package alc.journal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import alc.journal.utils.GlideApp;
import alc.journal.utils.Util;
import alc.journal.model.Journal;

public class JournalDetailActivity extends AppCompatActivity {

    private String mJournalEntryKey;
    private TextView mDetailTitle, mDetailDate, mDetailText;
    private ImageView mDetailImage;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_journal_detail);
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                mJournalEntryKey = bundle.getString(Util.JOURNAL_ENTRY_KEY);

            Toolbar toolbar =  findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ViewCompat.setTransitionName(findViewById(R.id.appBarLayout), "Name");

            CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

            collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

            this.setTitle("");
            mDetailTitle = findViewById(R.id.detail_title);
            mDetailDate = findViewById(R.id.detail_date);
            mDetailText = findViewById(R.id.detail_text);
            mDetailImage = findViewById(R.id.detail_image);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Util.DATA_BASE_NAME);
            Query connectedUser = mDatabase.child(mJournalEntryKey);
            connectedUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Journal mJournaSnapshot = dataSnapshot.getValue(Journal.class);
                    if (mJournaSnapshot != null) {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mJournaSnapshot.getmImageUrl());
                        GlideApp.with(JournalDetailActivity.this)
                                .load(storageReference)
                                .into(mDetailImage);
                        mDetailText.setText(mJournaSnapshot.getmText());
                        mDetailTitle.setText(mJournaSnapshot.getmTitle());
                        mDetailDate.setText(mJournaSnapshot.getmDate());
                        mTitle = mJournaSnapshot.getmTitle();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            collapsingToolbarLayout.setTitle(mTitle);
        } catch (Exception ex) {
            Util.showMessageBoxSimple(this, Util.APP_NAME, ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upd_journal, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_update:
                goToEditJournalActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToEditJournalActivity(){
        Intent intent = new Intent(JournalDetailActivity.this,AddJournalActivity.class);
        intent.putExtra(Util.EDIT_JOURNAL,true);
        intent.putExtra(Util.JOURNAL_ENTRY_KEY,mJournalEntryKey);
        startActivity(intent);
    }



}
