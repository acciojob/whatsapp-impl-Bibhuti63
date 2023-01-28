package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.

    private HashMap<Group, List<User>> groupUserMap;
    //to store Group and its users
    private HashMap<Group, List<Message>> groupMessageMap;
    //to store group and its massage's
    private HashMap<Message, User> senderMap;
    //to store massage by users present in group
    private HashMap<Group, User> adminMap;
    //to store group and its admin
    private HashSet<String> userMobile;
    //to store the users mobile numbers
    private int customGroupCount;
    //total group count (having more then 2 user)
    private int messageId;
    // total massage

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }


    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        else{
            User user=new User(name,mobile);
            userMobile.add(mobile);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users){
        if(users==null || users.size()<2){
            throw new IllegalArgumentException("A group must have 2 or more users");
        }
        User admin=users.get(0);
        if(users.size()==2){
            User otherUser=users.get(1);
            Group personalChat=new Group(otherUser.getName(),users.size());
            adminMap.put(personalChat,admin);
            groupUserMap.put(personalChat,users);
            return personalChat;
        }
        else{
            String groupName="Group "+(++customGroupCount);
            Group group=new Group(groupName,users.size());
            adminMap.put(group,admin);
            groupUserMap.put(group,users);
            return group;
        }
    }

    public int createMessage(String content){
        messageId++;
        Message message=new Message(messageId,content);
        return messageId;
    }


    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        List<User>senders=groupUserMap.get(group);

        if(!senders.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }

        List<Message> messages=groupMessageMap.get(group);
        if(messages==null){
            messages=new ArrayList<>();
        }

        messages.add(message);
        groupMessageMap.put(group,messages);

        senderMap.put(message,sender);

        return messages.size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!adminMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        User currentAdmin=adminMap.get(group);
        if(!currentAdmin.equals(approver)){
            throw new Exception("Approver does not have rights");
        }
        List<User>users=groupUserMap.get(group);
        if(!users.contains(user)){
            throw new Exception("User is not a participant");
        }

        adminMap.put(group,user);
        return "SUCCESS";

    }
}
