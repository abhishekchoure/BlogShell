package com.example.demo.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Blog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    private String title;
    @Column(columnDefinition = "text")
    private String description;
    private String author;
    private Date dateTime;
    private String category;
    private String pic;

    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",referencedColumnName = "id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "blog_users",
            joinColumns = {
                    @JoinColumn(name = "blog_id", referencedColumnName = "id",
                            nullable = false, updatable = false)},
            inverseJoinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "id",
                            nullable = false, updatable = false)})
    private Set<User> users = new HashSet<>();

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private Integer likes;

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getCategory() {
        return category;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Blog(Long id, String title, String description, String author, Date dateTime, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.dateTime = dateTime;
        this.category = category;
    }

    public Blog(Long id, String title, String description, String author, Date dateTime, String category, Integer likes, String pic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.dateTime = dateTime;
        this.category = category;
        this.likes = likes;
        this.pic = pic;
    }

    public Blog(Long id, String title, String description, String author, Date dateTime, String category, Integer likes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.dateTime = dateTime;
        this.category = category;
        this.likes = likes;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Blog(){}

    public Blog(Long id, Date dateTime, String title, String description, String author) {
        this.id = id;
        this.dateTime = dateTime;
        this.title = title;
        this.description = description;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", dateTime=" + dateTime +
                ", category='" + category + '\'' +
                ", likes=" + likes +
                '}';
    }

    @Transient
    public String getPhotosImagePath() {
        if (pic == null || id == null) return null;
        return "/user-photos/" + id + "/" + pic;
    }
}
