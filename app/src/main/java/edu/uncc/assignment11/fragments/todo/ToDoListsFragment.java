package edu.uncc.assignment11.fragments.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uncc.assignment11.R;
import edu.uncc.assignment11.databinding.FragmentToDoListsBinding;
import edu.uncc.assignment11.databinding.ListItemTodoListBinding;
import edu.uncc.assignment11.models.LoginResponse;
import edu.uncc.assignment11.models.ToDoList;
import edu.uncc.assignment11.models.TodoListResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ToDoListsFragment extends Fragment {
    public ToDoListsFragment() {
        // Required empty public constructor
    }

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    FragmentToDoListsBinding binding;
    ArrayList<ToDoList> mToDoLists = new ArrayList<>();
    ToDoListAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToDoListsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("ToDo Lists");

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.todo_lists_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.add_new_todo_list_action){
                    mListener.gotoCreateNewToDoList();
                    return true;
                } else if(menuItem.getItemId() == R.id.logout_action){
                    mListener.logout();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        adapter = new ToDoListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        getAllToDoListsForUser();
    }

    private void getAllToDoListsForUser() {
        //TODO: reload the todo lists for the currently logged in user
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("user_token", null);

        if (token == null) {
            Log.d(TAG, "getAllToDoListsForUser: No token found");
            return;
        }

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/api/todolists")
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
                        List<ToDoList> todoLists = todoListResponse.getTodoLists();
                        if (todoLists == null || todoLists.isEmpty()) {
                            Log.d(TAG, "TodoLists is empty or null!");

                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "No todo lists found", Toast.LENGTH_LONG).show();
                            });
                            return;
                        } else {
                            Log.d(TAG, "TodoLists size: " + todoLists.size());
                            Log.d(TAG, "TodoLists: " + todoLists);
                            requireActivity().runOnUiThread(() -> {
                                mToDoLists.clear();
                                mToDoLists.addAll(todoListResponse.getTodoLists());
                                adapter.notifyDataSetChanged();
                            });
                        }
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

    private void deleteToDoList(ToDoList toDoList) {
        //TODO: delete the todo list using the api
        //TODO: reload the todo lists for the currently logged in user
        Log.d(TAG, "deleteToDoList: " + toDoList.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete the " + toDoList.getName() + " todo list?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        String token = sharedPreferences.getString("user_token", null);

                        if (token == null) {
                            Log.d(TAG, "getAllToDoListsForUser: No token found");
                            return;
                        }

                        FormBody formBody = new FormBody.Builder()
                                .addEncoded("todolist_id", String.valueOf(toDoList.getTodolist_id()))
                                .build();

                        Request request = new Request.Builder()
                                .url("https://www.theappsdr.com/api/todolists/delete")
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
                                            getAllToDoListsForUser();
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
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }


    class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ToDoListViewHolder>{

        @NonNull
        @Override
        public ToDoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemTodoListBinding itemBinding = ListItemTodoListBinding.inflate(getLayoutInflater(), parent, false);
            return new ToDoListViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ToDoListViewHolder holder, int position) {
            ToDoList toDoList = mToDoLists.get(position);
            holder.bind(toDoList);
        }

        @Override
        public int getItemCount() {
            return mToDoLists.size();
        }

        class ToDoListViewHolder extends RecyclerView.ViewHolder{
            ListItemTodoListBinding itemBinding;
            ToDoList mToDoList;
            public ToDoListViewHolder(ListItemTodoListBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void bind(ToDoList toDoList) {
                mToDoList = toDoList;
                itemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.gotoToDoListDetails(toDoList);
                    }
                });

                itemBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteToDoList(mToDoList);
                    }
                });

                itemBinding.textViewName.setText(toDoList.getName());
            }
        }
    }

    ToDoListsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToDoListsListener) {
            mListener = (ToDoListsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ToDoListsListener");
        }
    }

    public interface ToDoListsListener {
        void gotoCreateNewToDoList();
        void gotoToDoListDetails(ToDoList toDoList);
        void logout();
    }
}