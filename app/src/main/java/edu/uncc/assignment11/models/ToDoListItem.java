package edu.uncc.assignment11.models;

public class ToDoListItem {
    String name;
    String priority;

    int todolist_item_id;

    public ToDoListItem() {
    }

    public ToDoListItem(String name, String priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getTodolist_item_id() {
        return todolist_item_id;
    }
}
