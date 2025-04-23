package edu.uncc.assignment11.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ToDoList implements Serializable {
    String name;

    int todolist_id;

    List<ToDoListItem> items = new ArrayList<>();

    public ToDoList() {
    }

    public List<ToDoListItem> getItems() {
        return items;
    }
    public int getTodolist_id() {
        return todolist_id;
    }

    public ToDoList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
