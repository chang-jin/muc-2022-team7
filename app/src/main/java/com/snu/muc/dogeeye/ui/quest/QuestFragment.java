package com.snu.muc.dogeeye.ui.quest;

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

import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.snu.muc.dogeeye.R;
import com.snu.muc.dogeeye.databinding.FragmentQuestBinding;

public class QuestFragment extends Fragment {

    private static final int RC_LEADERBOARD_UI = 9004;
    private QuestViewModel questViewModel;
    private FragmentQuestBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        questViewModel =
                new ViewModelProvider(this).get(QuestViewModel.class);

        binding = FragmentQuestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        questViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        displayLeaderboard();

        return root;
    }

    private void displayLeaderboard() {
        Log.d("Changjin", "displayLeaderBoard");

        try {
            PlayGames.getLeaderboardsClient(getActivity())
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
                            Log.d("Changjin", "" + e);
                        }
                    });
        } catch(Exception e) {
            Log.d("Changjin", "Error = " + e.getMessage());
        }
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