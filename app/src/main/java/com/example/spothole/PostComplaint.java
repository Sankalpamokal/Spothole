package com.example.spothole;



import static com.example.spothole.R.id.title;

public class PostComplaint {
    private String title;
    private String description;

    PostComplaint(){}

    public PostComplaint(String title, String description,String x) {
        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return "PostComplaint: " +
                "Title='" + title +
                ", Description='" + description ;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
