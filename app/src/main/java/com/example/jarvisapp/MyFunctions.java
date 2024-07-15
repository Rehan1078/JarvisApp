package com.example.jarvisapp;

import android.util.Log;

import java.util.Calendar;

public class MyFunctions {

    String apikey ="AIzaSyAIsUXO_TSb9uB2vWiLYbZV_BJO62SJckc";
    public static String wishing(){
        String msg = "";
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);

        if (time >= 0 && time < 6) {
            msg = "It's quite early, rise and shine!";
        } else if (time >= 6 && time < 12) {
            msg = "Good Morning! Have a great day ahead.";
        } else if (time >= 12 && time < 14) {
            msg = "Good Noon! Hope you're having a productive day.";
        } else if (time >= 14 && time < 16) {
            msg = "Good Afternoon! Don't forget to take a break.";
        } else if (time >= 16 && time < 18) {
            msg = "Good Evening! How was your day?";
        } else if (time >= 18 && time < 20) {
            msg = "Good Evening! It's time to unwind.";
        } else if (time >= 20 && time < 22) {
            msg = "Good Night! Have a relaxing evening.";
        } else if (time >= 22 && time < 24) {
            msg = "It's getting late, time to rest.";
        }

        return msg;
    }



    public static String fetchname(String msgs){
        String name="";
        boolean flag = false;
        String[] data = msgs.split(" ");
        for(int i = 0 ;i < data.length ; i++){
            String d = data[i];
            if(d.equals("call")){
                if(data[(i+1)].equals("to")){
                    flag = false;
                }else {
                    flag=true;
                }
            }else if(d.equals("and") || d.equals(".")){
                flag=false;

            }
            else if(data[(i-1)].equals("call")){
                if(d.equals("to")){
                    flag=true;
                }
            }

            if(flag){
                if(!d.equals("call") && !d.equals("to")){
                    name=name.concat(" "+d);
                }
            }
        }
        Log.d("JARVIS",name);

        return name;
    }

}
