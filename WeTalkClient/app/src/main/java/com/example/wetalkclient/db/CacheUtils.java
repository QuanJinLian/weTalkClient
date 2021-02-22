package com.example.wetalkclient.db;

import android.util.Log;

import com.example.wetalkclient.MainActivity;
import com.example.wetalkclient.bean.AddFriend;
import com.example.wetalkclient.bean.User;
import com.example.wetalkclient.constant.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CacheUtils {
    private static final String TAG = CacheUtils.class.getSimpleName();
    public static User mUser = null;

    public static void setUserCache(User user){
        mUser = user;
    }
    public static User getUserCache(){
        return mUser;
    }

    public static ArrayList<User> friendList = new ArrayList<User>() ;

    public static void setFriendList(String response){
        Document doc = Jsoup.parse(response);
        Elements result = doc.select("p.result");
        Elements userId = doc.select("ol > li.id");
        Elements nikName = doc.select("ol > li.nikName");
        Elements photo = doc.select("ol > li.photo");
        friendList.clear();
        for(int i = 0; i < result.size(); i++) {
            if (result.get(0).text().equals("친구있음")) {
                for(int j = 0; j < userId.size(); j++){
//                    Log.d(TAG, "id=" + userId.get(j).text() + ",,,nikName=" + nikName.get(j).text() + ",,,photo=" + photo.get(j).text());

                    Log.d(TAG, photo.get(j).text());
                    User user = new User().setUserId(userId.get(j).text())
                            .setName(nikName.get(j).text())
                            .setPhoto(photo.get(j).text());
                    friendList.add(user);
                }
            }
        }
    }
    public static ArrayList<User> getFriendList(){
        return friendList;
    }
    public static void addFriend(User user){
        friendList.add(user);
    }
    public static void removeFriend(User user){
        for(int i = 0; i < friendList.size(); i++){
            User u = friendList.get(i);
            if(u.getUserId().equals(user.getUserId())){
                friendList.remove(i);
                break;
            }
        }
    }
    public static User getFriend(String friendId){
        User user = new User();
        for(int i = 0; i < friendList.size(); i++){
            String id = friendList.get(i).getUserId();
            if(id.equals(friendId)){
                user = friendList.get(i);
                break;
            }
        }
        return user;
    }


    public static ArrayList<AddFriend> friendRequests  = new ArrayList<AddFriend>();
    public static void addFriendRequest(AddFriend user){
        friendRequests.add(user);
    }
    public static void removeFriendRequest(AddFriend user){
        String id = user.getFromId();
        for(int i = 0 ; i < friendRequests.size() ; i++){
            if(friendRequests.get(i).getFromId().equals(id)){
                friendRequests.remove(i);
            }
        }
    }
    public static ArrayList<AddFriend> getFriendRequests (){
        return friendRequests;
    }

}
