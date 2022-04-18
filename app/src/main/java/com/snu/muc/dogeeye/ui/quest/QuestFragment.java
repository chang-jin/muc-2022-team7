package com.snu.muc.dogeeye.ui.quest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.databinding.FragmentQuestBinding;

public class QuestFragment extends Fragment {

    private static final int RC_LEADERBOARD_UI = 9004;
    private Activity mActivity;
    private QuestViewModel questViewModel;
    private FragmentQuestBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        questViewModel =
                new ViewModelProvider(this).get(QuestViewModel.class);
        mActivity = getActivity();

        binding = FragmentQuestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        questViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        checkUserSignIn();

        return root;
    }

    private void displayLeaderboard() {
        Log.d("Changjin", "displayLeaderBoard");

        try {
            PlayGames.getLeaderboardsClient(mActivity)
                    .getLeaderboardIntent(getString(R.string.leaderboard_id))
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            Log.d("Changjin", "onSuccess");
                            startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Changjin", "onFailure : " + e);
                            Log.d("Changjin", "onFailure : " + e.getLocalizedMessage());
                            Log.d("Changjin", "onFailure : " + e.toString());
                            Log.d("Changjin", "onFailure : " + e.getStackTrace());
                        }
                    });
        } catch (Exception e) {
            Log.d("Changjin", "Error = " + e.getMessage());
        }
    }


    private void checkUserSignIn() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(mActivity);

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            Log.d("Changjin", "isAuthenticated : " + isAuthenticatedTask.isSuccessful());
            Log.d("Changjin", "isAuthenticated2 : " + isAuthenticatedTask.getResult().isAuthenticated());
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                Log.d("Changjin", "isAuthenticated");
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
                Log.d("Changjin", "signIn");
                gamesSignInClient.signIn();

            }
        });
        PlayGames.getPlayersClient(mActivity).getCurrentPlayer().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Changjin", "signIn done : " + task.getResult().getPlayerId());
                        displayLeaderboard();
                    }
                }
        );
    }

    private void submitScoreToLeaderboard() {
        PlayGames.getLeaderboardsClient(getActivity())
                .submitScore(getString(R.string.leaderboard_id), 1337);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}