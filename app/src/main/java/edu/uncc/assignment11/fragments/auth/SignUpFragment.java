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
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import edu.uncc.assignment11.databinding.FragmentSignupBinding;
import edu.uncc.assignment11.models.LoginResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SignUpFragment extends Fragment {
    public SignUpFragment() {
        // Required empty public constructor
    }

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    FragmentSignupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("User Sign Up");

        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoLogin();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = binding.editTextFirstName.getText().toString().trim();
                String lname = binding.editTextLastName.getText().toString().trim();
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString().trim();

                if(fname.isEmpty()){
                    Toast.makeText(getContext(), "First Name is required", Toast.LENGTH_SHORT).show();
                } else if(lname.isEmpty()){
                    Toast.makeText(getContext(), "Last Name is required", Toast.LENGTH_SHORT).show();
                } else if(email.isEmpty()){
                    Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()){
                    Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
                } else {
                    FormBody formBody = new FormBody.Builder()
                            .addEncoded("fname", fname)
                            .addEncoded("lname", lname)
                            .addEncoded("email", email)
                            .addEncoded("password", password)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://www.theappsdr.com/api/signup")
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
                                    mListener.onSignUpSuccessful();
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
                                        Toast.makeText(getContext(), "Sign Up Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                    });
                                } catch (JsonSyntaxException e) {
                                    // fallback in case parsing fails
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "Sign Up Failed: Unexpected error", Toast.LENGTH_LONG).show();
                                    });
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    SignUpListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SignUpListener) {
            mListener = (SignUpListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SignUpListener");
        }
    }

    public interface SignUpListener {
        void gotoLogin();
        void onSignUpSuccessful();
    }
}