package edu.uncc.assignment11.fragments.todo;

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

import java.io.IOException;
import java.util.List;

import edu.uncc.assignment11.databinding.FragmentCreateNewToDoListBinding;
import edu.uncc.assignment11.models.ToDoList;
import edu.uncc.assignment11.models.TodoListResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CreateNewToDoListFragment extends Fragment {
    public CreateNewToDoListFragment() {
        // Required empty public constructor
    }

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    FragmentCreateNewToDoListBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateNewToDoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create New List");
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelCreateNewToDoList();
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String listName = binding.editTextName.getText().toString().trim();
                if (listName.isEmpty()) {
                    Toast.makeText(getContext(), "List name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: Create new todo list using the api
                    CreateNewToDoListWithAPI(listName);
                }
            }
        });
    }

    public void CreateNewToDoListWithAPI(String listName) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("user_token", null);

        if (token == null) {
            Log.d(TAG, "getAllToDoListsForUser: No token found");
            return;
        }

        FormBody formBody = new FormBody.Builder()
                .addEncoded("name", listName)
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/api/todolists/create")
                .post(formBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    String jsonResponse = responseBody.string();
                    Log.d(TAG, "onResponse: " + jsonResponse);

                    Gson gson = new Gson();
                    TodoListResponse todoListResponse = gson.fromJson(jsonResponse, TodoListResponse.class);

                    if ("ok".equalsIgnoreCase(todoListResponse.getStatus())) {
                        requireActivity().runOnUiThread(() -> {
                            mListener.onSuccessCreateNewToDoList();
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Failed to load todo lists", Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "onResponse: " + body);
                }
            }
        });
    }

    CreateNewToDoListListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CreateNewToDoListListener) {
            mListener = (CreateNewToDoListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CreateNewToDoListListener");
        }
    }

    public interface CreateNewToDoListListener {
        void onSuccessCreateNewToDoList();
        void onCancelCreateNewToDoList();
    }
}