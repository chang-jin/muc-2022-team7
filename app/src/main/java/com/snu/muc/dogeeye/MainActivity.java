package com.snu.muc.dogeeye;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.snu.muc.dogeeye.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_photo, R.id.navigation_record, R.id.navigation_quest)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        PlayGamesSdk.initialize(this);

        checkUserSignIn();
    }

    private void checkUserSignIn() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(this);

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            Log.d("Changjin", "isAuthenticated : " + isAuthenticatedTask.isSuccessful());
            Log.d("Changjin", "isAuthenticated2 : " + isAuthenticatedTask.getResult().isAuthenticated());
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                Log.d("Changjin", "isAuthenticated");
                PlayGames.getPlayersClient(this).getCurrentPlayer().addOnCompleteListener(mTask -> {
                            // Get PlayerID with mTask.getResult().getPlayerId()
                            Log.d("Changjin", "get player");
//                    Log.d("Changjin", "get player : " + mTask.getResult().getPlayerId());

                        }
                );
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
                Log.d("Changjin", "signIn");
                gamesSignInClient.signIn();
            }
        });
    }

}