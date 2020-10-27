package com.mushin.muconnect.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mushin.muconnect.R;
import com.mushin.muconnect.databinding.FragmentPolarBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class PolarFragment extends Fragment {

    private PageViewModel pageViewModel;
    private FragmentPolarBinding binding;

    public static PolarFragment newInstance() {
        PolarFragment fragment = new PolarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(getActivity()).get(PageViewModel.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_polar, container, false);

        binding.setModel(pageViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        View root = binding.getRoot();

        return root;
    }

}
