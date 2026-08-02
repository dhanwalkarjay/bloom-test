package com.bloom.customer.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainSharedViewModel extends ViewModel {
    private final MutableLiveData<String> searchCategory = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();

    public void setSearchParams(String query, String category) {
        searchQuery.setValue(query);
        searchCategory.setValue(category);
    }

    public LiveData<String> getSearchCategory() {
        return searchCategory;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void clearSearchParams() {
        searchQuery.setValue(null);
        searchCategory.setValue(null);
    }
}
