package com.exmple.mymsappshometest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.exmple.mymsappshometest.adapters.FavoritesNewsRecyclerView;
import com.exmple.mymsappshometest.adapters.NewsRecyclerView;
import com.exmple.mymsappshometest.adapters.OnNewsListener;
import com.exmple.mymsappshometest.models.NewsModel;
import com.exmple.mymsappshometest.utils.Credentials;
import com.exmple.mymsappshometest.viewmodels.NewsListViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.List;
import java.util.concurrent.Executor;


public class FavoritesFragment extends Fragment implements OnNewsListener {

    private RecyclerView recyclerView;
    private FavoritesNewsRecyclerView favoritesNewsRecyclerViewAdapter;
    private NewsListViewModel newsListViewModel;

    private TextView signInTitle, getSignInSubtext;
    private ImageView googleSignInButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //todo:if user is NOT null user is  authenticated and recyclerview cab be visible now
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = rootView.findViewById(R.id.favorites_recyclerview);
        recyclerView.setVisibility(View.INVISIBLE);

        signInTitle = rootView.findViewById(R.id.sign_in_title);
        getSignInSubtext = rootView.findViewById(R.id.sign_in_subtext);
        googleSignInButton = rootView.findViewById(R.id.google_signin_button);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("3201732263-i6nmgnpq07bd45nqfpsgbsokfvfujh2h.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Not Working For Now I'm Working On It", Toast.LENGTH_SHORT).show();
                //launcher.launch(new Intent(mGoogleSignInClient.getSignInIntent()));
            }
        });

        newsListViewModel = new ViewModelProvider(this).get(NewsListViewModel.class);

        configureRecyclerView();
        observeAnyChange();
        searchNewsApi("articles");

        return rootView;
    }



    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Intent intent = result.getData();

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                    Toast.makeText(getContext(), "Please while we are getting your auth result...", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    Toast.makeText(getContext(), "Error accord: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    });

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(getContext(), "Not Authenticated : " + message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void observeAnyChange() {

        newsListViewModel.getNews().observe(getViewLifecycleOwner(), new Observer<List<NewsModel>>() {
            @Override
            public void onChanged(List<NewsModel> newsModels) {
                //Observing for any data changed
                if (newsModels != null) {
                    favoritesNewsRecyclerViewAdapter.setmNews(newsModels);
                }
            }
        });
    }

    private void searchNewsApi(String query) {
        newsListViewModel.searchNewsApi(query);
    }

    private void configureRecyclerView() {
        favoritesNewsRecyclerViewAdapter = new FavoritesNewsRecyclerView(this);
        recyclerView.setAdapter(favoritesNewsRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onNewsClick(int position) {
        NewsModel NewsModel = favoritesNewsRecyclerViewAdapter.getSelectedNews(position);

        Uri uri = Uri.parse(NewsModel.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onFavoritesClick(int position) {

    }
}