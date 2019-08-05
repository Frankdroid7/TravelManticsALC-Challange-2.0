package com.example.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class AdminActivity extends AppCompatActivity {
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText title_edTxt;
    EditText price_edTxt;
    EditText desc_edTxt;
    ImageView imageView;
    String userImageUri;
    TravelDeal deal;
    public static int PICTURE_RESULT = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

//        FirebaseUtil.openFbReference("traveldeals",UserActivity());
        mfirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        title_edTxt = findViewById(R.id.title_edTxt);
        price_edTxt = findViewById(R.id.price_edTxt);
        desc_edTxt = findViewById(R.id.desc_edTxt);
        imageView = findViewById(R.id.user_image);


        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("travelDeal");

        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        title_edTxt.setText(deal.getTitle());
        desc_edTxt.setText(deal.getDescription());
        price_edTxt.setText(deal.getPrice());

        showImage(deal.getImageUrl());
        Button btnImage = findViewById(R.id.choose_image_btn);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String url = ref.getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(url);
                    deal.setImageName(pictureName);
                    showImage(url);

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i("userImageUri", uri.toString());
                            userImageUri = uri.toString();
                        }
                    });

                    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                    Picasso.get()
                            .load(imageUri)
                            .resize(width, width * 2 / 3)
                            .centerCrop()
                            .into(imageView);

                }

            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_deal:
                saveDeal();
                Toast.makeText(getApplicationContext(), "Deal Saved", Toast.LENGTH_LONG).show();
                backToUserActivity();
                eraseFields();
                return true;
            case R.id.delete_deal:
                deleteDeal();
                Toast.makeText(getApplicationContext(), "Deal Deleted", Toast.LENGTH_LONG).show();
                backToUserActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveDeal() {

        deal.setTitle(title_edTxt.getText().toString());
        deal.setPrice(price_edTxt.getText().toString());
        deal.setDescription(desc_edTxt.getText().toString());
        deal.setImageUrl(userImageUri);
        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(getApplicationContext(), "Save the deal before deleting", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
    }

    private void backToUserActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    private void eraseFields() {
        title_edTxt.setText("");
        price_edTxt.setText("");
        desc_edTxt.setText("");
    }


    private void showImage(String url) {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        Picasso.get()
                .load(url)
                .resize(width, width * 2 / 3)
                .centerCrop()
                .into(imageView);

    }


}

