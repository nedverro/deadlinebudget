package com.example.deadlinebudget.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.deadlinebudget.fragments.BudgetFragment;
import com.example.deadlinebudget.fragments.NoteFragment;
import com.example.deadlinebudget.fragments.ScheduleFragment;
import com.example.deadlinebudget.fragments.TaskFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new TaskFragment();
            case 1: return new NoteFragment();
            case 2: return new ScheduleFragment();
            case 3: return new BudgetFragment();
            default: return new TaskFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}