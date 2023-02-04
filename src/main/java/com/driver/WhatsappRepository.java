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

    public Group createGroup(List<User> users) throws Exception {
        if(users==null || users.size()<2){
            throw new Exception("A group must have 2 or more users");
        }
        User admin=users.get(0);
        if(users.size()==2){
            //this is a personal chat
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

    public int removeUser(User user) throws Exception {
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group,
        // remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return
        // (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        //checking if user is present in any of group
        boolean flag=false;
        Group current_group=null;
        for(Group group : groupUserMap.keySet()){
            List<User> list=groupUserMap.get(group);
            if(list.contains(user)){
                flag=true;
                current_group=group;
                break;
            }
        }
        if(flag==false){
            throw  new Exception("User not found");
        }
        //check if user is admin
        User admin=adminMap.get(current_group);
        if(Objects.equals(admin,user)){
            throw  new Exception("Cannot remove admin");
        }

        //remove user from group
        groupUserMap.remove(user);

        //removing message by user sent in group
        List<Message> groupMessage=groupMessageMap.get(current_group);
        List<Message> temp=new ArrayList<>();
        for(Message message: groupMessage){
            if(senderMap.get(message).equals(user)){
                continue;
            }
            temp.add(message);
        }
        groupMessageMap.put(current_group,temp);

        //delete massage by the user
        for(Message message: senderMap.keySet()){
            User u=senderMap.get(message);
            if(Objects.equals(user,u)){
                senderMap.remove(message);
            }
        }

        //updating the number of user in group
        int userCount=current_group.getNumberOfParticipants();
        current_group.setNumberOfParticipants(userCount-1);
        //update num of message in group
        int messageCount=groupMessageMap.get(current_group).size();
        //updated num of overal message
        int overallMessageCount=messageId-1;

        return (userCount-1)+(messageCount)+(overallMessageCount);

    }

    public String findMessage(Date start, Date end, int k) throws Exception {

        //finding all message between given time
        List<Message>list=new ArrayList<>();
        for(Message message : senderMap.keySet()){
            Date current_date=message.getTimestamp();
            //-Ve: first date is before the second date.
            //Zero: two dates are equal.
            //+Ve: first date is after the second date.
            if((start.compareTo(current_date)<=0)&&(end.compareTo(current_date)>=0)){
                list.add(message);
            }
        }
        //if num of msg(excluding start and end) < K throw exception
        int n=list.size();
        if((n-2)<k){
            throw new Exception("K is greater than the number of messages");
        }

        //we have to sort the messeges in Descending order  of time
        Collections.sort(list, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        //return Kth latest message between given time (excluding start, end)

        /*DRY RUN Example:
            ex: n=10;//size of message
            given k=4;
            (10,9,8,7,6,5,4,3,2,1)
            So: the Kth latest message (excluding start end will be):6 which is at index 4
         */

        String ans=list.get(k).getContent();
        return ans;
    }
}
