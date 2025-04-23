package edu.uncc.assignment11.fragments.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import edu.uncc.assignment11.R;
import edu.uncc.assignment11.databinding.FragmentListDetailsBinding;
import edu.uncc.assignment11.databinding.ListItemListItemBinding;
import edu.uncc.assignment11.models.ToDoList;
import edu.uncc.assignment11.models.ToDoListItem;
import edu.uncc.assignment11.models.TodoListDetailsResponse;
import edu.uncc.assignment11.models.TodoListResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ToDoListDetailsFragment extends Fragment {
    private static final String ARG_PARAM_TODO_LIST= "ARG_PARAM_TODO_LIST";
    FragmentListDetailsBinding binding;
    private ToDoList mToDoList;

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "demo";

    public static ToDoListDetailsFragment newInstance(ToDoList toDoList) {
        ToDoListDetailsFragment fragment = new ToDoListDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_TODO_LIST, toDoList);
        fragment.setArguments(args);
        return fragment;
    }

    public ToDoListDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mToDoList = (ToDoList) getArguments().getSerializable(ARG_PARAM_TODO_LIST);
        }
    }

    ArrayList<ToDoListItem> mToDoListItems = new ArrayList<>();
    ToDoListItemAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("ToDo Lists");

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.todo_list_details_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.add_new_list_item_action){
                    mListener.gotoAddListItem(mToDoList);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goBackToToDoLists();
            }
        });

        adapter = new ToDoListItemAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        loadToDoListItems();
        if (mToDoList != null) {
            String name = "For " + mToDoList.getName();
            binding.textViewListName.setText(name);
        }
    }

    void loadToDoListItems(){
        //TODO: Load the items for the to do list using the api

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("user_token", null);

        if (token == null) {
            Log.d(TAG, "getAllToDoListsForUser: No token found");
            return;
        }

        String url = "https://www.theappsdr.com/api/todolists/" + mToDoList.getTodolist_id();


        Request request = new Request.Builder()
                .url(url)
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
                    TodoListDetailsResponse todoListResponse = gson.fromJson(jsonResponse, TodoListDetailsResponse.class);

                    if ("ok".equalsIgnoreCase(todoListResponse.getStatus())) {
                        ToDoList toDoList = todoListResponse.getTodolist();
                        if (toDoList != null) {
                            Log.d(TAG, "onResponse: ToDoList: " + toDoList);
                            if (toDoList.getItems() == null || toDoList.getItems().isEmpty()) {
                                Log.d(TAG, "TodoList is empty or null!");

                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "No todo items found", Toast.LENGTH_LONG).show();
                                });
                                return;
                            } else {
                                Log.d(TAG, "TodoList size: " + toDoList.getItems().size());
                                Log.d(TAG, "TodoList: " + toDoList);
                                requireActivity().runOnUiThread(() -> {
                                    mToDoListItems.clear();
                                    mToDoListItems.addAll(toDoList.getItems());
                                    adapter.notifyDataSetChanged();
                                });
                            }
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

    void deleteToDoListItem(ToDoListItem toDoListItem){
        //TODO: Delete the item using the api
        //TODO: Reload the items for the to do list using the api

        Log.d(TAG, "deleteToDoListItem: " + toDoListItem.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete " + toDoListItem.getName() + "?")
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
                                .addEncoded("todolist_id", String.valueOf(mToDoList.getTodolist_id()))
                                .addEncoded("todolist_item_id", String.valueOf(toDoListItem.getTodolist_item_id()))
                                .build();

                        Request request = new Request.Builder()
                                .url("https://www.theappsdr.com/api/todolist-items/delete")
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
                                            loadToDoListItems();
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

    class ToDoListItemAdapter extends RecyclerView.Adapter<ToDoListItemAdapter.ToDoListItemViewHolder>{

        @NonNull
        @Override
        public ToDoListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ListItemListItemBinding itemBinding = ListItemListItemBinding.inflate(getLayoutInflater(), parent, false);
            return new ToDoListItemViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ToDoListItemViewHolder holder, int position) {
            ToDoListItem toDoListItem = mToDoListItems.get(position);
            holder.bind(toDoListItem);
        }

        @Override
        public int getItemCount() {
            return mToDoListItems.size();
        }

        class ToDoListItemViewHolder extends RecyclerView.ViewHolder{
            ListItemListItemBinding itemBinding;
            ToDoListItem mToDoListItem;
            public ToDoListItemViewHolder(ListItemListItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void bind(ToDoListItem toDoListItem) {
                this.mToDoListItem = toDoListItem;

                itemBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteToDoListItem(mToDoListItem);
                    }
                });

                itemBinding.textViewName.setText(toDoListItem.getName());
                itemBinding.textViewPriority.setText(toDoListItem.getPriority());
            }
        }
    }

    ToDoListDetailsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ToDoListDetailsListener) {
            mListener = (ToDoListDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ToDoListDetailsListener");
        }
    }

    public interface ToDoListDetailsListener {
        void gotoAddListItem(ToDoList toDoList);
        void goBackToToDoLists();
    }
}