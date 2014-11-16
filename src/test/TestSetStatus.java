package test;

import java.util.List;

import crawl.yun.User;
import crawl.yun.util.UserSet;

public class TestSetStatus {
  public static void main(String[] args) {
//    UserSet.getInstance().setStatus(656323722L, "running1");
    List<User> users = UserSet.getInstance().getStatusUsers("running2", 10);
    for (User user : users) {
      System.out.println(user);
    }
  }
}
