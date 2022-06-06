package com.snu.muc.dogeeye.ui;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.snu.muc.dogeeye.MainActivity;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.databinding.ActivityMainBinding;
import com.snu.muc.dogeeye.databinding.ActivitySuggestBinding;

public class SuggestActivity extends AppCompatActivity {

 int totalSug = 3;
 String[] sugName = new String[totalSug];
 String[] sugDescription = new String[totalSug];
 String[] sugLink = new String[totalSug];
 String[] sugGood = new String[totalSug];
 String[] sugBad = new String[totalSug];
 String[] sugPhotoPath = new String[totalSug];

 private ActivitySuggestBinding binding;
 boolean sug0_expand = false;
 boolean sug1_expand = false;
 boolean sug2_expand = false;


 String givenURL = "";
 Intent goToWebPageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(givenURL));
// startActivity(goToWebPageIntent);


 @Override
 protected void onCreate(Bundle savedInstanceState){

  Intent sug0WebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.sug0_link)));
  Intent sug1WebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.sug1_link)));
  Intent sug2WebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.sug2_link)));

  super.onCreate(savedInstanceState);
  binding = ActivitySuggestBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());

  binding.sug0Img.setOnClickListener(view -> {
   startActivity(sug0WebIntent);
  });

  binding.sug1Img.setOnClickListener(view -> {
   startActivity(sug1WebIntent);
  });

  binding.sug2Img.setOnClickListener(view -> {
   startActivity(sug2WebIntent);
  });


  AppCompatButton sug0Tv = binding.sug0Text;
  AppCompatButton sug1Tv = binding.sug1Text;
  AppCompatButton sug2Tv = binding.sug2Text;

  binding.sug0Text.setOnClickListener(view -> {
   if(sug0_expand){
    sug0Tv.setText(this.getString(R.string.sug0_click));
    sug0_expand = false;
   }
   else{
    sug0Tv.setText(this.getString(R.string.sug0));
    sug0_expand = true;
    sug1_expand = false;
    sug2_expand = false;
    sug1Tv.setText(this.getString(R.string.sug1_click));
    sug2Tv.setText(this.getString(R.string.sug2_click));
   }
  });


  binding.sug1Text.setOnClickListener(view -> {
   if(sug1_expand){
    sug1Tv.setText(this.getString(R.string.sug1_click));
    sug1_expand = false;
   }
   else{
    sug1Tv.setText(this.getString(R.string.sug1));
    sug1_expand = true;
    sug0_expand = false;
    sug2_expand = false;
    sug0Tv.setText(this.getString(R.string.sug0_click));
    sug2Tv.setText(this.getString(R.string.sug2_click));
   }
  });


  binding.sug2Text.setOnClickListener(view -> {
   if(sug2_expand){
    sug2Tv.setText(this.getString(R.string.sug2_click));
    sug2_expand = false;
   }
   else{
    sug2Tv.setText(this.getString(R.string.sug2));
    sug2_expand = true;
    sug0_expand = false;
    sug1_expand = false;
    sug0Tv.setText(this.getString(R.string.sug0_click));
    sug1Tv.setText(this.getString(R.string.sug1_click));
   }
  });



 }



}
