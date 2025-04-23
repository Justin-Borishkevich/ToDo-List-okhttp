package edu.uncc.assignment11.models;

public class TodoListDetailsResponse {
    private String status;
    private ToDoList todolist;

    public String getStatus() {
        return status;
    }

    public ToDoList getTodolist() {
        return todolist;
    }
}
