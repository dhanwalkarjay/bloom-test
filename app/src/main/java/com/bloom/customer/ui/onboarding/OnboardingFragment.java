package com.bloom.customer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bloom.R;
import com.bloom.databinding.FragmentOnboardingBinding;

public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        int position = getArguments() != null ? getArguments().getInt("position", 0) : 0;

        switch (position) {
            case 0:
                binding.ivIllustration.setImageResource(R.drawable.hero_login_floral);
                binding.tvTitle.setText("Master Artisans");
                binding.tvDescription.setText("Discover the best local florists hand-picked for their exceptional quality and unmatched creativity.");
                break;
            case 1:
                binding.ivIllustration.setImageResource(R.drawable.hero_occasions);
                binding.tvTitle.setText("Bespoke Creations");
                binding.tvDescription.setText("Design a bouquet as unique as the person receiving it, guided by our intuitive AI assistant.");
                break;
            case 2:
                binding.ivIllustration.setImageResource(R.drawable.hero_signup_floral);
                binding.tvTitle.setText("Endless Bloom");
                binding.tvDescription.setText("Bring nature indoors with our exclusive weekly and monthly fresh floral subscriptions.");
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
