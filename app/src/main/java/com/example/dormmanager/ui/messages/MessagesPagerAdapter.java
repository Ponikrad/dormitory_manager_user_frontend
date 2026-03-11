package com.example.dormmanager.ui.messages;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MessagesPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 2;

    public MessagesPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminMessagesFragment();
            case 1:
                return new ResidentChatFragment();
            default:
                return new AdminMessagesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public String getPageTitle(int position) {
        switch (position) {
            case 0:
                return "📧 Admin";
            case 1:
                return "🏠 Residents";
            default:
                return "";
        }
    }
}

