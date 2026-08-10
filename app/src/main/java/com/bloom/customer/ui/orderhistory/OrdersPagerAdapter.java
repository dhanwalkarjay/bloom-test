package com.bloom.customer.ui.orderhistory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OrdersPagerAdapter extends FragmentStateAdapter {

    public OrdersPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // position 0 = Active, position 1 = Past
        return OrderListFragment.newInstance(position == 1);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
