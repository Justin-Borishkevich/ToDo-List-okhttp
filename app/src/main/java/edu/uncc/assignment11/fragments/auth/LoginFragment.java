package edu.uncc.assignment11.fragments.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.uncc.assignment11.databinding.FragmentLoginBinding;
import edu.uncc.assignment11.models.LoginResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginFragment extends Fragment {
    public LoginFragment() {
        // Required empty public constructor
    }

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    FragmentLoginBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("User Login");
        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Enter Name!!", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()){
                    Toast.makeText(getActivity(), "Enter Password!!", Toast.LENGTH_SHORT).show();
                } else {
//                    {
//                        "status": "ok",
//                            "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3NDQ4MDY1MjMsImV4cCI6MTc3NjM0MjUyMywianRpIjoiMllTSDFraXVFQ09abXdkbGg0emlINiIsInVzZXIiOjE2Mn0.IXyNnL6i35BfLqUnFrH1O4X8pY71HI_eH11QcTihe38",
//                            "user_id": 162,
//                            "user_email": "test@t.com",
//                            "user_fname": "Test",
//                            "user_lname": "Testing",
//                            "user_role": "USER"
//                    }

                    FormBody formBody = new FormBody.Builder()
                            .addEncoded("email", email)
                            .addEncoded("password", password)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://www.theappsdr.com/api/login")
                            .post(formBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if(response.isSuccessful()) {
                                ResponseBody responseBody = response.body();
                                String jsonResponse = responseBody.string();

                                Gson gson = new Gson();
                                LoginResponse loginResponse = gson.fromJson(jsonResponse, LoginResponse.class);

                                String token = loginResponse.getToken();

                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("user_token", token);
                                editor.apply();

                                Log.d(TAG, "Stored token: " + token);

                                requireActivity().runOnUiThread(() -> {
                                    mListener.onLoginSuccessful();
                                });

                            } else {
                                ResponseBody responseBody = response.body();
                                String body = responseBody.string();
                                Log.d(TAG, "onResponse: " + body);

                                Gson gson = new Gson();
                                try {
                                    LoginResponse errorResponse = gson.fromJson(body, LoginResponse.class);
                                    String errorMessage = errorResponse.getMessage();

                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Sign in Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                    });
                                } catch (JsonSyntaxException e) {
                                    // fallback in case parsing fails
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Sign in Failed: Unexpected error", Toast.LENGTH_LONG).show();
                                    });
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });

        binding.buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.gotoSignUpUser();
            }
        });

    }

    LoginListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LoginListener) {
            mListener = (LoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginListener");
        }
    }

    public interface LoginListener {
        void gotoSignUpUser();
        void onLoginSuccessful();
    }
}