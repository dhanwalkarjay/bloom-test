package com.bloom.customer.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bloom.customer.data.local.SessionManager;
import com.bloom.customer.ui.home.HomeActivity;
import com.bloom.databinding.ActivityOnboardingBinding;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        com.bloom.customer.util.SystemBarInsets.apply(this);

        OnboardingAdapter adapter = new OnboardingAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new com.google.android.material.tabs.TabLayoutMediator(binding.dotsIndicator, binding.viewPager,
                (tab, position) -> {}).attach();

        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
        
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    binding.btnNext.setText("Get Started");
                } else {
                    binding.btnNext.setText("Next");
                }
            }
        });
    }

    private void finishOnboarding() {
        SessionManager.getInstance(this).setFirstLaunch(false);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
