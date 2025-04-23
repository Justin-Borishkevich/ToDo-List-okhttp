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

import edu.uncc.assignment11.R;
import edu.uncc.assignment11.databinding.FragmentAddItemToToDoListBinding;
import edu.uncc.assignment11.models.ToDoList;
import edu.uncc.assignment11.models.TodoListResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AddItemToToDoListFragment extends Fragment {
    private static final String ARG_PARAM_TODO_LIST = "ARG_PARAM_TODO_LIST";
    private ToDoList mTodoList;

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    public AddItemToToDoListFragment() {
        // Required empty public constructor
    }

    public static AddItemToToDoListFragment newInstance(ToDoList toDoList) {
        AddItemToToDoListFragment fragment = new AddItemToToDoListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_TODO_LIST, toDoList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTodoList = (ToDoList) getArguments().getSerializable(ARG_PARAM_TODO_LIST);
        }
    }

    FragmentAddItemToToDoListBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddItemToToDoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Add Item to List");
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelAddItemToList(mTodoList);
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = binding.editTextName.getText().toString().trim();
                if (itemName.isEmpty()) {
                    Toast.makeText(getContext(), "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    String priority = "Low";
                    int checkedId = binding.radioGroup.getCheckedRadioButtonId();
                    if(checkedId == R.id.radioButtonMedium){
                        priority = "Medium";
                    } else if(checkedId == R.id.radioButtonHigh){
                        priority = "High";
                    }
                    //TODO: Add new todo list item to the list using the api

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("user_token", null);

                    if (token == null) {
                        Log.d(TAG, "getAllToDoListsForUser: No token found");
                        return;
                    }

                    FormBody formBody = new FormBody.Builder()
                            .addEncoded("todolist_id", String.valueOf(mTodoList.getTodolist_id()))
                            .addEncoded("name", itemName)
                            .addEncoded("priority", priority)
                            .build();

                    Request request = new Request.Builder()
                            .url("https://www.theappsdr.com/api/todolist-items/create")
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
                                        mListener.onSuccessAddItemToList();
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
            }
        });


    }

    AddItemToListListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddItemToListListener) {
            mListener = (AddItemToListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AddItemToListListener");
        }
    }

    public interface AddItemToListListener{
        void onSuccessAddItemToList();
        void onCancelAddItemToList(ToDoList todoList);
    }
}