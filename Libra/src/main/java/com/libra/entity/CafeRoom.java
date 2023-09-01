package com.libra.entity;

import java.util.ArrayList;
import java.util.List;

public class CafeRoom {

  public static final String NAME = "name";
  public static final String DISTANCE = "distance";
  public static final String COUNT_USER = "countUser";
  public static final String COUNT_FEMALE = "countFemale";

  private String id;
  private String name;
  private double distance;
  private List<User> users = new ArrayList<>();
  private int countFemale;
  private String address;

  public CafeRoom() {
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users.clear();
    this.users.addAll(users);
  }

  public int getCountUser() {
    return users.size();
  }

  public int getCountMale() {
    return getCountUser() - countFemale;
  }

  public int getCountFemale() {
    return countFemale;
  }

  public void setCountFemale(int countFemale) {
    this.countFemale = countFemale;
  }

  public String getAddress() {
    return this.address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public boolean isEmpty() {
    return name == null;
  }
}
