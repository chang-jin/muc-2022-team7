package com.snu.muc.dogeeye.ui.quest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.snu.muc.dogeeye.common.Logger;
import com.snu.muc.dogeeye.databinding.FragmentQuestBinding;

public class QuestFragment extends Fragment {

    private static final Logger log = new Logger();
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_ACHIEVEMENT_UI = 9003;
    private Activity mActivity;
    private FragmentQuestBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();

        binding = FragmentQuestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button leaderBoard = binding.leaderboardBtn;
        final Button achievement = binding.achievementBtn;
        leaderBoard.setOnClickListener(view -> displayLeaderboard());
        achievement.setOnClickListener(view -> showAchievements());
        checkUserSignIn();

        return root;
    }

    private void displayLeaderboard() {
        log.d("displayLeaderBoard");
        try {
            PlayGames.getLeaderboardsClient(mActivity)
                    .getLeaderboardIntent(getString(R.string.leaderboard_id))
                    .addOnSuccessListener(intent -> {
                        log.d("onSuccess");
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    })
                    .addOnFailureListener(e -> log.d("onFailure"));
        } catch (Exception e) {
            log.e("Error = " + e.getMessage());
        }
    }

    private void checkUserSignIn() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(mActivity);

        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());

            if (isAuthenticated) {
                // Continue with Play Games Services
                binding.leaderboardBtn.setVisibility(View.VISIBLE);
            } else {
                binding.signInBtn.setOnClickListener(view -> gamesSignInClient.signIn());
                binding.signInBtn.setVisibility(View.VISIBLE);
            }
        });
        PlayGames.getPlayersClient(mActivity).getCurrentPlayer().
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        binding.welcome.setText(task.getResult().getDisplayName() + " Welcome!");
                        binding.leaderboardBtn.setVisibility(View.VISIBLE);
                        binding.achievementBtn.setVisibility(View.VISIBLE);
//                        submitScoreToLeaderboard();
                    }
                }
        );
    }

    private void submitScoreToLeaderboard() {
        PlayGames.getLeaderboardsClient(mActivity)
                .submitScore(getString(R.string.leaderboard_id), 80);
        PlayGames.getLeaderboardsClient(mActivity)
                .submitScore(getString(R.string.leaderboard_id), 50);
        PlayGames.getLeaderboardsClient(mActivity)
                .submitScore(getString(R.string.leaderboard_id), 100);
    }


    private void showAchievements() {
        PlayGames.getAchievementsClient(mActivity)
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}