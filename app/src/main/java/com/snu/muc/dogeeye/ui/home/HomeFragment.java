package com.snu.muc.dogeeye.ui.home;

import static com.snu.muc.dogeeye.MainActivity.main_stt;
import static com.snu.muc.dogeeye.MainActivity.main_tts;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.snu.muc.dogeeye.databinding.FragmentHomeBinding;

import android.widget.Button;
import android.widget.EditText;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
//    public ttsModule main_tts;

    private Map<String, String> stt_map;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView listenText = binding.testListenResult;
        final Button button0 = binding.button0;
        final Button button1 = binding.button1;
        final Button button2 = binding.button2;


        final EditText editTextTab = binding.editTextTab;

        button0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                main_tts.speak(editTextTab.getText().toString());
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("BTN", "button1 clicked");
                String return_text = main_stt.listen2(getContext());
                listenText.setText(return_text);

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("BTN", "button2 clicked");
                int text_to_code = main_stt.listen_and_give_code(getContext(), 0);

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}